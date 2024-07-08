package com.epay.ewallet.store.daesang.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.epay.ewallet.store.daesang.constant.AppConstant;
import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.constant.LogCategory;
import com.epay.ewallet.store.daesang.constant.MultilingualMessagesConstant;
import com.epay.ewallet.store.daesang.mapperOne.IDucVietCart;
import com.epay.ewallet.store.daesang.mapperOne.IStoreProduct;
import com.epay.ewallet.store.daesang.model.DucVietCart;
import com.epay.ewallet.store.daesang.model.Ecode;
import com.epay.ewallet.store.daesang.model.Store;
import com.epay.ewallet.store.daesang.request.AddProductToCartRequest;
import com.epay.ewallet.store.daesang.request.ChangeCartProductQuantityRequest;
import com.epay.ewallet.store.daesang.request.ChooseCartProductRequest;
import com.epay.ewallet.store.daesang.request.CreateOrderRequest;
import com.epay.ewallet.store.daesang.request.MobileCartCheckoutRequest;
import com.epay.ewallet.store.daesang.request.ViewCartRequest;
import com.epay.ewallet.store.daesang.response.CartProductDetailResponse;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.epay.ewallet.store.daesang.utility.CodeService;
import com.epay.ewallet.store.daesang.utility.Utils;
import com.google.gson.Gson;

@Service
public class CartService {
	
	private static final Logger logger = LogManager.getLogger(CartService.class);
    private static final Gson gson = new Gson();
	
    @Autowired private IStoreProduct iStoreProduct;
    @Autowired private IDucVietCart iDucVietCart;
    
    @Autowired private StoreService storeService;
    @Autowired private OrderService orderService;
    @Autowired private CodeService codeService;
    
    @Value("${DAESANG_STORE_ID}") private long DAESANG_STORE_ID;
    @Value("${order.service.create.order.api}") private String CREATE_ORDER_API;
    
    
    public CommonResponse<Object> addProductToCart(AddProductToCartRequest request, long companyId, long userId,
    		String requestId) throws Exception {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_ADD_PRODUCT_TO_CART;
    	
    	/**************** VALIDATION ****************/
    	response = storeService.generalValidate(companyId, DAESANG_STORE_ID, logCategory, requestId);
    	response.setData(null); //after general validation, data will be set by the store's information
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
    		return response;
		}
    	
    	if (request.getQuantity() <= 0) {
			logger.info("{} | {} | Invalid request: Quantity must be greater than 0", requestId, logCategory);
			response.setEcode(EcodeConstant.FAIL_INVALID_PRODUCT_QUANTITY);
			return response;
		}
    	
    	//Verify if the request productId is wrong
    	if (request.getProductId() < 0) {
    		logger.info("{} | {} | Request ProductId is invalid(less than 0)", logCategory, requestId);
			response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_FOUND);
			return response;
		}
    	
    	Map<String, String> productInfo = iStoreProduct.getProductCodeByID(request.getProductId());
    	if (productInfo == null || productInfo.isEmpty()
    			|| Utils.isStringEmpty(productInfo.get("PRODUCT_CODE"))) {
    		logger.info("{} | {} | Product is inactive or not found by productId = {}", logCategory, requestId, request.getProductId());
			response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_FOUND);
			return response;
		}
    	
    	//Check if user's cart is full
    	int totalProductInUserCart = iDucVietCart.countTotalProductInUserCart(userId);
    	if (totalProductInUserCart >= AppConstant.USER_CART_LIMITATION) {
    		logger.info("{} | {} | User's cart is full, can't add more product", logCategory, requestId);
			response.setEcode(EcodeConstant.FAIL_USER_CART_IS_FULL);
			return response;
		}
    	
    	/************** END VALIDATION **************/
    	
    	/************ PROCESSING REQUEST ************/
    	DucVietCart cartProduct = iDucVietCart.getCartProductByID(userId + productInfo.get("PRODUCT_CODE"));
    	logger.info("{} | {} | Get current cart product from DB: {}", requestId, logCategory, gson.toJson(cartProduct));
    	
    	//If the product is not in the cart -> add new
    	if (cartProduct == null) {
			cartProduct = DucVietCart.builder()
					.id(userId + productInfo.get("PRODUCT_CODE"))
					.userId(new BigDecimal(userId))
					.productId(productInfo.get("PRODUCT_CODE"))
					.quantity(request.getQuantity())
					.build();
			
			//Insert to DB
			iDucVietCart.addNewProductToCart(cartProduct);
		}
    	//If the product is already in the cart -> update quantity(add to current quantity)
    	else {
    		cartProduct.setQuantity(cartProduct.getQuantity() + request.getQuantity());
    		iDucVietCart.changeQuantity(cartProduct);
    	}
    	logger.info("{} | {} | Add product to cart is successful: {}", requestId, logCategory, gson.toJson(cartProduct));
    	
    	return response;
    }
    
    
    public CommonResponse<Object> viewCart(ViewCartRequest request, long companyId, long userId, String language, String requestId) {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_VIEW_CART;
    	
    	/**************** VALIDATION ****************/
    	response = storeService.generalValidate(companyId, DAESANG_STORE_ID, logCategory, requestId);
    	response.setData(null); //after general validation, data will be set by the store's information
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
    		return response;
		}
    	/************** END VALIDATION **************/
    	
    	/************ PROCESSING REQUEST ************/
    	int page = request.getPage() < 0 ? 0 : request.getPage();
    	int start = page * AppConstant.PRODUCTS_PER_PAGE;
		int end = start + AppConstant.PRODUCTS_PER_PAGE;
		
    	List<Map<String, Object>> cartDetail = iDucVietCart.getUserCartDetail(userId, start, end);
    	List<CartProductDetailResponse> cartProductDetailResponses = new ArrayList<>();
    	
    	//Cart is empty
    	if (cartDetail == null || cartDetail.isEmpty()) {
			logger.info("{} | {} | User's cart is empty");
			response.setData(cartProductDetailResponses);
		}
    	//Cart is not empty
    	else {
			cartDetail.forEach(
					cd -> cartProductDetailResponses.add(
						CartProductDetailResponse.builder()
							.productCode(cd.get("PRODUCT_ID").toString())
							.name(cd.get("NAME").toString())
							.image(cd.get("IMAGE").toString())
							.price(cd.get("DISCOUNT_PRICE").toString())
							.originalPrice(cd.get("PRICE").toString())
							.quantity(Integer.valueOf(cd.get("QUANTITY").toString()))
							.message(Integer.valueOf(cd.get("STATUS").toString()) == AppConstant.PRODUCT_STATUS_ACTIVE ?
									"" : mappingErrorMsgProductInactive(language))
							.build()
					));
			response.setData(cartProductDetailResponses);
		}
    	
    	return response;
    }
    
    private String mappingErrorMsgProductInactive(String language) {
    	String message = "";
    	switch (language) {
		case AppConstant.USER_LANG_VN:
			message = MultilingualMessagesConstant.VN_CART_PRODUCT_NOT_ACTIVE;
			break;
		case AppConstant.USER_LANG_EN:
			message = MultilingualMessagesConstant.EN_CART_PRODUCT_NOT_ACTIVE;
			break;
		case AppConstant.USER_LANG_KR:
			message = MultilingualMessagesConstant.KR_CART_PRODUCT_NOT_ACTIVE;
			break;
		default:
			message = MultilingualMessagesConstant.EN_CART_PRODUCT_NOT_ACTIVE;
			break;
		}
    	return message;
    }
    
    
    public CommonResponse<Object> changeProductQuantity(ChangeCartProductQuantityRequest request, long companyId, long userId, String requestId) {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_CHANGE_PRODUCT_QUANTITY;
    	
    	/**************** VALIDATION ****************/
    	response = storeService.generalValidate(companyId, DAESANG_STORE_ID, logCategory, requestId);
    	response.setData(null); //after general validation, data will be set by the store's information
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
    		return response;
		}
    	
    	if (request.getNewQuantity() < 0) {
			logger.info("{} | {} | Can't update product quantity to negative value | Request quantity = {}", requestId,
					logCategory, request.getNewQuantity());
			response.setEcode(EcodeConstant.FAIL_INVALID_PRODUCT_QUANTITY);
			return response;
		}
    	
    	//Check if the requested product code is in the user's cart
    	DucVietCart cartProduct = iDucVietCart.getCartProductByID(userId + request.getProductCode());
    	logger.info("{} | {} | Get current cart product from DB: {}", requestId, logCategory, gson.toJson(cartProduct));
    	if (cartProduct == null) {
			logger.info("{} | {} | Requested productCode={} | Not found in the user's cart", requestId, logCategory, request.getProductCode());
			response.setEcode(EcodeConstant.FAIL_PRODUCT_IS_NOT_IN_CART);
			return response;
		}
    	/************** END VALIDATION **************/
    	    	
    	/************ PROCESSING REQUEST ************/
    	//Only update cartProduct.quantity if the new quantity is different compared to the old quantity
    	if (request.getNewQuantity() != cartProduct.getQuantity()) {
			
    		//If the new quantity is 0 -> remove the product from user's cart
    		if (request.getNewQuantity() == 0) {
				logger.info("{} | {} | Request quantity = 0 | Remove the product from the user's cart", requestId, logCategory);
				iDucVietCart.removeProductFromCart(cartProduct.getId());
				logger.info("{} | {} | Remove product is done", requestId, logCategory);
			}
    		//If the new quantity is > 0 -> update the product's quantity
    		else {
    			cartProduct.setQuantity(request.getNewQuantity());
    			iDucVietCart.changeQuantity(cartProduct);
    			logger.info("{} | {} | Update new quantity of product is successful: {}", requestId, logCategory, gson.toJson(cartProduct));
    		}
		}
    	
    	return response;
    }
    
    
    /**
     * Calculating the total amount when user choose product in their cart 
     * @param request
     * @param userId
     * @param requestId
     * @return
     */
    public CommonResponse<Object> chooseProductInCart(ChooseCartProductRequest request, long userId, String requestId) {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_CHOOSE_PRODUCT_IN_CART;
    	
    	/**************** VALIDATION ****************/
    	if (request.getChosenList() == null || request.getChosenList().isEmpty()) {
    		logger.info("{} | {} | The chosen list of Products is empty", requestId, logCategory);
			response.setEcode(EcodeConstant.ERROR);
			return response;
		}
    	/************** END VALIDATION **************/
    	
    	/************ PROCESSING REQUEST ************/
    	Map<String, BigDecimal> sumCartProductAmount;
    	//Read data from DB
    	sumCartProductAmount = iDucVietCart.sumCartProductAmount(userId, request.getChosenList());
    	
    	//Data from DB is null
    	if (sumCartProductAmount == null || sumCartProductAmount.isEmpty()) {
			logger.info("{} | {} | Products are not found by the list of chosen product code", requestId, logCategory);
			response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_FOUND);
			return response;
		}
    	
    	//Return to mobile
    	Map<String, String> data = new HashMap<>();
    	data.put("totalExpectedOrderValue", String.valueOf(sumCartProductAmount.get("TOTAL_DISCOUNT_PRICE").longValue()));
    	data.put("totalExpectedOriginalValue", String.valueOf(sumCartProductAmount.get("TOTAL_ORIGINAL_PRICE").longValue()));
    	response.setData(data);
    	response.setEcode(EcodeConstant.SUCCESS);
    	return response;
    }
    
    
    public CommonResponse<Object> cartCheckout(MobileCartCheckoutRequest request, long userId, long companyId,
    		String requestId, Map<String, String> header) throws NumberFormatException, Exception {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_CART_CHECKOUT;
    	String language = header.get("language");
    	Store store = null;
    	
    	/**************** VALIDATION ****************/
    	response = storeService.generalValidate(companyId, request.getStoreId(), logCategory, requestId);
    	//Get store from response after passed all of validation
    	store = (Store) response.getData();
    	//after general validation, data will be set by the store's information
    	response.setData(null); 
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
    		return response;
		}
    	
    	if (request.getProducts() == null || request.getProducts().isEmpty()) {
			logger.info("{} | {} | The list of products in request is null or empty", requestId, logCategory);
			response.setEcode(EcodeConstant.CART_CHECKOUT_LIST_PRODUCT_EMPTY);
			return response;
		}
    	
    	/** Validate cart data for checkout **/
    	//1. Get list product code from list product in request
    	List<String> listProductCodeRequest = new ArrayList<>();
    	for (MobileCartCheckoutRequest.CartProduct cartProduct : request.getProducts()) {
    		if (Utils.isStringEmpty(cartProduct.productCode)) {
    			logger.info("{} | {} | Request's list cartProduct has an element with empty productCode", requestId, logCategory);
				response.setEcode(EcodeConstant.CART_CHECKOUT_LIST_PRODUCT_INVALID);
				return response;
			}
    		listProductCodeRequest.add(cartProduct.productCode);
    	}
    	
    	//2. Load cart product data from DB by list of product codes in request then validate it
    	List<Map<String, Object>> cartProductDataCheckout = iDucVietCart.loadListCartProductCheckout(userId, listProductCodeRequest);
    	if (cartProductDataCheckout == null || cartProductDataCheckout.isEmpty()) {
    		logger.info("{} | {} | Load list cartProductCheckout from DB is empty", requestId, logCategory);
			response.setEcode(EcodeConstant.CART_CHECKOUT_LIST_PRODUCT_INVALID);
			return response;
		} else if (listProductCodeRequest.size() != cartProductDataCheckout.size()) {
			logger.info("{} | {} | Total cart product data from DB does not match the total product code in the request", requestId, logCategory);
			response.setEcode(EcodeConstant.CART_CHECKOUT_LIST_PRODUCT_INVALID);
			return response;
		} else {
			//Compare the order quantity of each product and check product status
			for (Map<String, Object> productData : cartProductDataCheckout) {
				
				//Compare the order quantity of each product
				for (MobileCartCheckoutRequest.CartProduct cartProduct : request.getProducts()) {
					if (cartProduct.productCode.equals(productData.get("PRODUCT_CODE").toString())) {
						int productQuantity = Integer.valueOf(productData.get("QUANTITY").toString());
						if (cartProduct.quantity != productQuantity) {
							logger.info("{} | {} | Request product code = {}, quantity = {} | Not match data from DB: quantity = {}",
									requestId, logCategory, cartProduct.quantity, productQuantity);
							response.setEcode(EcodeConstant.CART_CHECKOUT_LIST_PRODUCT_INVALID);
							return response;
						}
					}
				}
				
				//Check product status | If violated this validation the mobile client will show Popup.
				if (Integer.valueOf(productData.get("STATUS").toString()) != AppConstant.PRODUCT_STATUS_ACTIVE) {
					logger.info("{} | {} | Product: {} is inactive | Error Case: Some products stoped selling.",
							requestId, logCategory, productData.get("PRODUCT_CODE").toString());
					response.setEcode(EcodeConstant.CART_SOME_PRODUCTS_STOPED_SELLING);
					return response;
				}
			}
		}
    	
    	//Check valid purchase time of each product | If violated this validation the mobile client will show Popup.
    	for (Map<String, Object> productData : cartProductDataCheckout) {
			response = orderService.checkPurchaseTime(null, userId, requestId, logCategory, productData);
			if (response.getEcode() != EcodeConstant.SUCCESS) {
				return response;
			}
		}
    	
    	// Check the user's purchase threshold for classes 2 and 3 products during the month
    	// If violated this validation the mobile client will show Popup.
    	// Getting total weight of list of products are purchasing from cart checkout which is class 2 or 3
    	long totalWeightPurchasing = 0;
    	for (Map<String, Object> productData : cartProductDataCheckout) {
    		if (Integer.valueOf(productData.get("PRODUCT_CLASS").toString()) > 1) {
    			long productWeight = Long.valueOf(productData.get("WEIGHT").toString());
    			int quantity = Integer.valueOf(productData.get("QUANTITY").toString());
				totalWeightPurchasing += (productWeight * quantity);
			}
    	}
    	// Check purchase limit
    	if (totalWeightPurchasing > 0) {
	    	response = orderService.checkPurchaseLimitByProductWeight(userId, requestId, logCategory, totalWeightPurchasing);
	    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
	    		Ecode ecode = codeService.getEcode(response.getEcode(), language);
				response.setMessage(ecode.getMessage().replace("?", response.getData().toString()));
				response.setP_ecode(ecode.getP_ecode());
				response.setP_message(ecode.getP_message());
				response.setData(null);
				return response;
			}
    	}
    	/************** END VALIDATION **************/
    	
    	/************ PROCESSING REQUEST ************/
    	//Build order request
    	CreateOrderRequest createOrderRequest = orderService.buildCreateOrderRequest(null, userId, requestId, logCategory, store,
    			null, cartProductDataCheckout);
    	
    	//Call createOrder API from order-service
    	CommonResponse<Object> createOrderResponse = orderService.getConnectOrderService(gson.toJson(createOrderRequest), CREATE_ORDER_API, header);
    	logger.info("{} | {} | Order-service response = {}", requestId, logCategory, gson.toJson(response));
    	
    	//Order-service response create order successful
    	if (createOrderResponse.getEcode().equals(EcodeConstant.SUCCESS)) {
    		cartProductDataCheckout = iDucVietCart.loadListCartProductCheckoutSuccess(userId, listProductCodeRequest);
    		
    		response = orderService.handleSuccessfulResponseRromOrderService(null, null,
					createOrderRequest, createOrderResponse, language, cartProductDataCheckout);
    	} else {
    		response = createOrderResponse;
    	}
    	logger.info("{} | {} | Create order for user's cart is done | Response={}", requestId, logCategory, gson.toJson(response));
    	
    	return response;
    }
    
}
