package com.epay.ewallet.store.daesang.service;

import com.epay.ewallet.store.daesang.constant.AppConstant;
import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.constant.LogCategory;
import com.epay.ewallet.store.daesang.mapperOne.IStore;
import com.epay.ewallet.store.daesang.mapperOne.IStoreProduct;
import com.epay.ewallet.store.daesang.mapperOne.IUser;
import com.epay.ewallet.store.daesang.model.*;
import com.epay.ewallet.store.daesang.mapperOne.ICompany;
import com.epay.ewallet.store.daesang.mapperOne.IDucVietCart;
import com.epay.ewallet.store.daesang.request.GetProductDetailsRequest;
import com.epay.ewallet.store.daesang.request.GetStoreDeliverAddressRequest;
import com.epay.ewallet.store.daesang.request.SearchProductsRequest;
import com.epay.ewallet.store.daesang.response.DeliveryAddressResponse;
import com.epay.ewallet.store.daesang.response.VisitStoreResponse;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.epay.ewallet.store.daesang.response.GetProductsDetailsResponse;
import com.epay.ewallet.store.daesang.response.ProductImages;
import com.epay.ewallet.store.daesang.response.StoreBannerResponse;
import com.epay.ewallet.store.daesang.response.StoreFixedDeliveryAddressResponse;
import com.epay.ewallet.store.daesang.response.StoreProductResponse;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoreService {
	private static final Logger logger = LogManager.getLogger(StoreService.class);
	private static final Gson gson = new Gson();

	@Value("${DAESANG_COMPANY_ID}") private long DAESANG_COMPANY_ID;
	@Value("${DAESANG_STORE_ID}") private long DAESANG_STORE_ID;
	
	@Autowired private IUser iUser;
	@Autowired private IStore iStore;
	@Autowired private ICompany iCompany;
	@Autowired private IStoreProduct iStoreProduct;
	@Autowired private IDucVietCart iDucVietCart;
	

	public CommonResponse<Object> visitDaesangStore(long userId, long userCompanyId, String logCategory, String requestId) {
		CommonResponse<Object> response = new CommonResponse<>();
		VisitStoreResponse visitStoreResponse = new VisitStoreResponse();
		try {
			response = generalValidate(userCompanyId, DAESANG_STORE_ID, logCategory, requestId);
			if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
				response.setData(null);
				return response;
			}
			
			//Get store from response after passed all of validation
			Store daesangStore = (Store) response.getData();

			//Get list of store's banners
			List<Banner> banners = iStore.getBannersByStoreIdSortByPriorityAsc(DAESANG_STORE_ID);
			List<StoreBannerResponse> listBannerResponse = new ArrayList<>();
			if (banners != null && !banners.isEmpty()) {
				listBannerResponse = banners.stream()
						.map(banner -> new StoreBannerResponse(banner.getId(), banner.getImage(), banner.getPriority()))
						.collect(Collectors.toList());
			}
			
			//Get the current total cart item by UserId
			Map<String, BigDecimal> currentTotalCartItem = iDucVietCart.countTotalCartItemByUserId(userId);
			
			visitStoreResponse.setStoreId(daesangStore.getId());
			visitStoreResponse.setStoreName(daesangStore.getName());
			visitStoreResponse.setBanners(listBannerResponse);
			visitStoreResponse.setCurrentTotalCartItem(currentTotalCartItem.get("total_cart_item").longValue());
			
			response.setData(visitStoreResponse);
			response.setEcode(EcodeConstant.SUCCESS);
		} catch (Exception e) {
			logger.fatal("{} | {} | Exception during processing request access store..", logCategory, requestId, e);
			response.setEcode(EcodeConstant.EXCEPTION);
			response.setData(null);
		}

		return response;
	}


	public CommonResponse<Object> getStoreDeliveryAddress(GetStoreDeliverAddressRequest request, long userId, long userCompanyId, String requestId) {
		CommonResponse<Object> response = new CommonResponse<>();
		String logCategory = LogCategory.LOG_GET_STORE_DELIVERY_ADDRESS;
		try {
			response = generalValidate(userCompanyId, request.getStoreId(), logCategory, requestId);			
			if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
				response.setData(null);
				return response;
			}
			
			//Get User info from DB
			User user = iUser.getUserById(userId);
			if (user == null) {
				logger.info("{} | {} | User not found by id={}", requestId, logCategory, userId);
				response.setEcode(EcodeConstant.FAIL_USER_NOT_FOUND);
				response.setData(null);
				return response;
			}
			
			String username = user.getName() == null ? "" : user.getName();
			if (username.trim().isEmpty()) {
				Map<String, String> userFullName = iUser.getUserFullNameByPhone(user.getPhoneNumber());
				logger.info("{} | {} | UserName from TBL_USERS is null -> get UserName from TBL_COMPANY_USER: {}", requestId, logCategory, gson.toJson(userFullName));
				username = userFullName.get("FULL_NAME") == null ? "" : userFullName.get("FULL_NAME"); 
			}
					
			//Get store from response after passed all of validation
			Store daesangStore = (Store) response.getData();

			// get store's delivery address
			List<StoreFixedDeliveryAddress> deliveryAddress = iStore.getDeliveryAddressByStoreId(request.getStoreId());
			List<StoreFixedDeliveryAddressResponse> listDeliveryAddress = new ArrayList<>();
			if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
				listDeliveryAddress = deliveryAddress.stream()
						.map(address -> new StoreFixedDeliveryAddressResponse(address.getId(), address.getName(), address.getAddress()))
						.collect(Collectors.toList());
			}
			
			DeliveryAddressResponse deliveryAddressResponse = DeliveryAddressResponse.builder()
					.storeId(daesangStore.getId())
					.storeName(daesangStore.getName())
					.username(username)
					.phone(user.getPhoneNumber())
					.deliveryAddress(listDeliveryAddress)
					.build();

			response.setData(deliveryAddressResponse);
			response.setEcode(EcodeConstant.SUCCESS);
			return response;

		} catch (Exception e) {
			logger.fatal("{} | {} | Exception during processing request get store delivery address..", logCategory,
					requestId, e);
			response.setEcode(EcodeConstant.EXCEPTION);
			response.setData(null);
			return response;
		}

	}

	public CommonResponse<Object> searchProducts(Long userCompanyId, SearchProductsRequest request, String logCategory,
			String requestId) {
		CommonResponse<Object> response = new CommonResponse<>();
		response = generalValidate(userCompanyId, request.getStoreId(), logCategory, requestId);
		response.setData(null);
		if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
			return response;
		}
		
		try {
			int page = request.getPage() < 0 ? 0 : request.getPage();
			int start = page * AppConstant.PRODUCTS_PER_PAGE;
			int end = start + AppConstant.PRODUCTS_PER_PAGE + 1;

			List<Product> products;
			if (request.getProductName() == null || request.getProductName().isEmpty()) {
				/*****************************
				 * Get products list if missing product name request
				 *****************************/
				products = iStoreProduct.getAllActiveProductsInPage(request.getStoreId(), start, end);
			} else {
				/*****************************
				 * search products list by name (product name will toUpperCase before query to
				 * DB)
				 *****************************/

				products = iStoreProduct.getAllActiveProductsByNameInPage(request.getStoreId(),
						request.getProductName().toUpperCase(), start, end);
			}
			
			List<StoreProductResponse> listProductResponse = new ArrayList<>();
			if (products != null && !products.isEmpty()) {
				products.forEach(product -> {
					listProductResponse.add(StoreProductResponse.builder()
							.id(product.getId())
							.image(product.getImage())
							.name(product.getName())
							.originPrice(String.valueOf(product.getOriginPrice()))
							.discountPrice(String.valueOf(product.getDiscountPrice()))
							.discountPercent(product.getDiscountPercent() + "%")
							.build());
				});
			}
			
			
			logger.info("{} | {} | Products={}", logCategory, requestId, gson.toJson(products));

			response.setEcode(EcodeConstant.SUCCESS);
			response.setData(listProductResponse);
		} catch (Exception e) {
			logger.fatal("{} | {} | Exception during processing request search products by name..", logCategory,
					requestId, e);
			response.setEcode(EcodeConstant.EXCEPTION);
			response.setData(null);
		}

		return response;
	}

	public CommonResponse<Object> getProductDetailsById(long userId, long userCompanyId, GetProductDetailsRequest request,
			String logCategory, String requestId) {
		CommonResponse<Object> response = new CommonResponse<>();
		Map<String, Object> data = new HashMap<>();
		
		response = generalValidate(userCompanyId, request.getStoreId(), logCategory, requestId);
		if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
			response.setData(null);
			return response;
		}

		try {
			/*****************************
			 * Get product info
			 *****************************/
			List<ProductDetail> productDetails = iStoreProduct.getProductsById(request.getProductId(), request.getStoreId());

			/*****************************
			 * Validate product
			 *****************************/
			if (productDetails == null || productDetails.isEmpty()) {
				logger.fatal("{} | {} | Product is not found", logCategory, requestId);
				response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_FOUND);
				response.setData(null);
				return response;
			}

			if (productDetails.get(0) == null || productDetails.get(0).getStatus() == 0) {
				logger.fatal("{} | {} | Product is not active", logCategory, requestId);
				response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_ACTIVE);
				response.setData(null);
				return response;
			}

			// convert product details to response
			GetProductsDetailsResponse product = GetProductsDetailsResponse.builder()
					.id(productDetails.get(0).getId())
					.images(
						// loop all data and map images and priorities into ProductImages object
						productDetails.stream()
								.map(p -> ProductImages.builder().priority(p.getPriority())
										.link(p.getLink()).build())
								.collect(Collectors.toList()))
					.name(productDetails.get(0).getName())
					.originPrice(String.valueOf(productDetails.get(0).getOriginPrice()))
					.discountPrice(String.valueOf(productDetails.get(0).getDiscountPrice()))
					.discountPercent(productDetails.get(0).getDiscountPercent() + "%")
					.productDetails(productDetails.get(0).getProductDetails())
					.build();
			logger.info("{} | {} | Product={}", logCategory, requestId, gson.toJson(product));
			
			//Get the current total cart item by UserId
			Map<String, BigDecimal> currentTotalCartItem = iDucVietCart.countTotalCartItemByUserId(userId);
			
			//Set data response
			data.put("product", product);
			data.put("currentTotalCartItem", currentTotalCartItem.get("total_cart_item").longValue());
			
			response.setEcode(EcodeConstant.SUCCESS);
			response.setData(data);
		} catch (Exception e) {
			logger.fatal("{} | {} | Exception during processing request get product details..", logCategory, requestId,
					e);
			response.setEcode(EcodeConstant.EXCEPTION);
			response.setData(null);
		}

		return response;
	}
	
	
	/**
	 * @param response
	 * @param userCompanyId
	 * @param logCategory
	 * @param requestId
	 * @return
	 */
	public CommonResponse<Object> generalValidate(long userCompanyId, long storeId, String logCategory, String requestId) {
		CommonResponse<Object> response = new CommonResponse<>();
		
		//Validate request storeId
		if (storeId != DAESANG_STORE_ID) {
			logger.info("{} | {} | Invalid request storeId", logCategory, requestId);
			response.setEcode(EcodeConstant.FAIL_INVALID_STORE_ID);
			return response;
		}
		
		Store daesangStore = iStore.getStoreById(storeId);
		Company daesangCompany = iCompany.getCompanyById(DAESANG_COMPANY_ID);

		// check if the user is DAESANG employee or not, if not -> return error
		if (userCompanyId != DAESANG_COMPANY_ID) {
			logger.info("{} | {} | User is not DAESANG employee", logCategory, requestId);
			response.setEcode(EcodeConstant.NOT_DAESANG_EMPLOYEE);
			return response;
		}

		// check if DAESANG store is active or not, if not -> return error
		if (daesangStore == null) {
    		logger.info("{} | {} | Store not found by storeId={}", logCategory, requestId, storeId);
			response.setEcode(EcodeConstant.FAIL_STORE_NOT_FOUND);
			return response;
		} else if (daesangStore.getStatus() != AppConstant.STORE_STATUS_ACTIVE) {
			logger.info("{} | {} | Store is not active", logCategory, requestId);
			response.setEcode(EcodeConstant.STORE_INACTIVE);
			return response;
		}
		logger.info("{} | {} | Store={}", logCategory, requestId, gson.toJson(daesangStore));
		
		// check if DAESANG company is active or not, if not -> return error
		if (daesangCompany == null || daesangCompany.getStatus() != AppConstant.COMPANY_STATUS_ACTIVE) {
			logger.info("{} | {} | DAESANG company is not active", logCategory, requestId);
			response.setEcode(EcodeConstant.COMPANY_INACTIVE);
			return response;
		}

		// Passed all validation
		response.setEcode(EcodeConstant.SUCCESS);
		response.setData(daesangStore);
		return response;
	}
}
