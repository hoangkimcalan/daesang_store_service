package com.epay.ewallet.store.daesang.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
//import com.sun.tools.javac.comp.Check;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;

import com.epay.ewallet.store.daesang.constant.AppConstant;
import com.epay.ewallet.store.daesang.constant.EcodeConstant;
import com.epay.ewallet.store.daesang.constant.LogCategory;
import com.epay.ewallet.store.daesang.constant.PaymentOptionConstant;
import com.epay.ewallet.store.daesang.entities.BuyOnDayOfWeekConfiguration;
import com.epay.ewallet.store.daesang.entities.Order;
import com.epay.ewallet.store.daesang.mapperOne.IDucVietCart;
import com.epay.ewallet.store.daesang.mapperOne.IOrder;
import com.epay.ewallet.store.daesang.mapperOne.IStore;
import com.epay.ewallet.store.daesang.mapperOne.IStoreProduct;
import com.epay.ewallet.store.daesang.mapperOne.IUser;
import com.epay.ewallet.store.daesang.model.Ecode;
import com.epay.ewallet.store.daesang.model.Store;
import com.epay.ewallet.store.daesang.model.StoreFixedDeliveryAddress;
import com.epay.ewallet.store.daesang.model.User;
import com.epay.ewallet.store.daesang.request.CreateOrderRequest;
import com.epay.ewallet.store.daesang.request.GetAllOrderRequest;
import com.epay.ewallet.store.daesang.request.GetOrderDetailRequest;
import com.epay.ewallet.store.daesang.request.MobileBuyNowRequest;
import com.epay.ewallet.store.daesang.request.OrderProductRequest;
import com.epay.ewallet.store.daesang.request.Receiver;
import com.epay.ewallet.store.daesang.request.RetryPlaceOrderRequest;
import com.epay.ewallet.store.daesang.request.SelectDeliveryAddressRequest;
import com.epay.ewallet.store.daesang.response.CommonResponse;
import com.epay.ewallet.store.daesang.response.CreateOrderResponseData;
import com.epay.ewallet.store.daesang.response.OrderDetailResponse;
import com.epay.ewallet.store.daesang.response.OrderPayByMegaVDetailResponse;
import com.epay.ewallet.store.daesang.response.OrderProductResponse;
import com.epay.ewallet.store.daesang.response.OrderResponse;
import com.epay.ewallet.store.daesang.response.SelectedDeliveryAddressResponse;
import com.epay.ewallet.store.daesang.utility.CodeService;
import com.epay.ewallet.store.daesang.utility.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@Service
public class OrderService {

    private static final Logger logger = LogManager.getLogger(OrderService.class);
    private static final Gson gson = new Gson();

    @Autowired private IOrder iOrder;
    @Autowired private IStore iStore;
    @Autowired private IStoreProduct iStoreProduct;
    @Autowired private IUser iUser;
    @Autowired private IDucVietCart iDucVietCart;
    
    @Autowired private StoreService storeService;
    @Autowired private CodeService codeService;

    @Value("${epay.merchant.group.id}")			private String EPAY_MERCHANT_GROUP_ID;
    @Value("${daesang.service.id}")				private String DAESANG_SERVICE_ID;
    @Value("${order.service.create.order.api}") private String CREATE_ORDER_API;
    @Value("${order.service.get.order.api}")	private String GET_ORDER_API;
    @Value("${icon.payment.option.megav}")		private String ICON_PAYMENT_MEGAV;
    @Value("${icon.payment.option.cash}")		private String ICON_PAYMENT_CASH;
    @Value("${DAESANG_STORE_ID}")				private long DAESANG_STORE_ID;
    
    /**
     * this threshold applies to all products in classes 2 and 3/1 user/1 month
     * unit: gram | apply validation if value >=0
     */
    @Value("${max_weight_per_month_for_product_class_2_3}")
    private long PURCHASE_LIMIT_BY_WEIGHT;
    

    public CommonResponse<Object> getAllOrders(long userId, long userCompanyId, GetAllOrderRequest request, String logCategory, String requestId) {
        CommonResponse<Object> response = new CommonResponse<>();
        /*****************************
         * Validation
         *****************************/
        response = storeService.generalValidate(userCompanyId, request.getStoreId(), logCategory, requestId);
        if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
        	response.setData(null);
			return response;
		}
        
        List<String> status = Arrays.asList(AppConstant.STT_TO_CONFIRM, AppConstant.STT_COMPLETED, AppConstant.STT_CANCELED);
		//check if order status is valid or not, if not -> return error
        if (request.getOrderStatus().isEmpty() || !status.contains(request.getOrderStatus())) {
            logger.info("{} | {} | Status is not found", logCategory, requestId);
            response.setEcode(EcodeConstant.FAIL_ORDER_STATUS_NOT_FOUND);
            response.setData(null);
            return response;
        }

        try {
        	String orderStatusRequest = convertOrderStatusRequest(request.getOrderStatus());
            int page = request.getPage() < 0 ? 0 : request.getPage();
            int start = page * AppConstant.PRODUCTS_PER_PAGE;
            int end = start + AppConstant.PRODUCTS_PER_PAGE + 1;
            
            logger.info("{} | {} | Start get list Order from DB: start={} | end={}", requestId, logCategory, start, end);
            List<Map<String, Object>> orderList = iOrder.getOrders(request.getStoreId(), DAESANG_SERVICE_ID, orderStatusRequest, userId, start, end);
            List<OrderResponse> orderResponses = new ArrayList<>();
            logger.info("{} | {} | Get list Order from DB is done: List Size={}", requestId, logCategory, orderList.size());
            
            if (orderList != null && !orderList.isEmpty()) {
            	for (Map<String, Object> order : orderList) {
            		long orderAmount = 0;
            		Map<String, Object> paymentMethod = gson.fromJson(String.valueOf(order.get("PMT_METHOD")),
            				new TypeToken<Map<String, Object>>() {}.getType());
            		
            		if (orderStatusRequest.equals(AppConstant.ORDER_STT_PROCESSING)
            				|| orderStatusRequest.equals(AppConstant.ORDER_STT_CANCELED)) {
            			orderAmount = paymentMethod.get("balances").toString().equals(PaymentOptionConstant.ID_CASH) ? 
            					Long.valueOf(order.get("AMOUNT").toString()) : Long.valueOf(order.get("PAYMENT_AMOUNT").toString());
					} else {
						orderAmount = Long.valueOf(order.get("TOTAL_AMOUNT").toString()) - Long.valueOf(order.get("REFUND_AMOUNT").toString());
					}
            		
            		OrderResponse orderResponse = OrderResponse.builder()
            				.orderId(order.get("ORDER_ID").toString())
            				.productImage(order.get("IMAGE").toString())
            				.productName(order.get("PRODUCT_NAME").toString())
            				.orderAmount(String.valueOf(orderAmount))
            				.status(request.getOrderStatus())
            				.totalProduct(Integer.valueOf(order.get("TOTAL_PRODUCT").toString()))
            				.build();
            		orderResponses.add(orderResponse);
            	}
			}

            response.setData(orderResponses);
            response.setEcode(EcodeConstant.SUCCESS);
        } catch (Exception e) {
            logger.fatal("{} | {} | Exception during processing request access store..", logCategory, requestId, e);
            response.setEcode(EcodeConstant.EXCEPTION);
            response.setData(null);
        }

        return response;
    }
    

    public CommonResponse<Object> getOrderDetail(long userId, long userCompanyId, GetOrderDetailRequest request, String logCategory,
    		String requestId, Map<String, String> header) throws Exception {
        CommonResponse<Object> response = new CommonResponse<>();
        
        /*************** VALIDATION ***************/
        response = storeService.generalValidate(userCompanyId, DAESANG_STORE_ID, logCategory, requestId);
        if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
        	response.setData(null);
			return response;
		}
        
        if (request.getOrderId() == null || request.getOrderId().isEmpty()) {    
        	logger.info("{} | {} | Request OrderID is null or empty", logCategory, requestId);
            response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
            response.setData(null);
            return response;
        }
        
        //Call Order-service to get Order detail
        Order order = getOrder(requestId, logCategory, request.getOrderId(), header);
        if (order == null) {
        	logger.info("{} | {} | Request orderId={} | Order not found", requestId, logCategory, request.getOrderId());
        	response.setEcode(EcodeConstant.FAIL_ORDER_NOT_FOUND);
            response.setData(null);
            return response;
		}
        
        //Check if userId is valid
        if (order.userId == null || order.userId.longValue() != userId) {
            logger.info("{} | {} | User is not order owner or order.userId is null", logCategory, requestId);
            response.setEcode(EcodeConstant.FAIL_USER_NOT_ORDER_OWNER);
            response.setData(null);
            return response;
        }
        
        //Check order payment method selected
        if(order.selectedPaymentMethods == null || order.selectedPaymentMethods.isEmpty()) {
        	logger.info("{} | {} | Order is not valid: selected payment method is empty", logCategory, requestId);
            response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
            response.setData(null);
            return response;
        }
        
        //Validate order status
        if (order.status == null || order.status.isEmpty()
        		|| (!order.status.equals(AppConstant.ORDER_STT_PROCESSING)
        				&& !order.status.equals(AppConstant.ORDER_STT_RECEIVED)
        				&& !order.status.equals(AppConstant.ORDER_STT_CANCELED))) {
        	logger.info("{} | {} | Order is not valid: Order status is invalidate", logCategory, requestId);
            response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
            response.setData(null);
            return response;
		}
        
        
        /*********** PROCESSING REQUEST ***********/
        //Preparing response data
        OrderDetailResponse orderDetailResponse;
        SimpleDateFormat orderTimeFormat = new SimpleDateFormat(AppConstant.ORDER_CREATED_TIME_FORMAT);
        Map<String, String> deliveryInfo = new HashMap<>();
        deliveryInfo.put("username", order.receiver.name);
    	deliveryInfo.put("phone", order.receiver.phone);
    	deliveryInfo.put("address", order.deliveryAddress);
    	deliveryInfo.put("deliveryTime", order.estimateDeliveryTime);
    	
    	Map<String, Object> productCodeParam = new HashMap<>();
    	List<String> listProductCode = new ArrayList<>();
    	order.products.forEach(product -> listProductCode.add(product.id));
    	productCodeParam.put("productCode", listProductCode);
    	List<Map<String, Object>> productInfoList = iStoreProduct.getOrderProductInfoByListCode(productCodeParam, order.orderId);
    	
    	List<OrderProductResponse> productList = new ArrayList<>();
    	long deliveryAmount = order.amount.longValue();
    	for(Map<String, Object> product : productInfoList) {
    		int orderQuantity = new BigDecimal(product.get("QUANTITY").toString()).intValue();
    		int canceledQuantity = product.get("CANCELED_QUANTITY") == null ?
    				0 : new BigDecimal(product.get("CANCELED_QUANTITY").toString()).intValue();
    		OrderProductResponse productResponse = OrderProductResponse.builder()
    				.productCode(String.valueOf(product.get("PRODUCT_CODE")))
    				.name(String.valueOf(product.get("NAME")))
    				.image(String.valueOf(product.get("IMAGE")))
    				.price(String.valueOf(product.get("DISCOUNT_PRICE")))
    				.originalPrice(String.valueOf(product.get("ORIGINAL_PRICE")))
    				.quantity(orderQuantity)
    				.deliveryQuantity(orderQuantity - canceledQuantity)
    				.build();
    		productList.add(productResponse);
    		
    		if (canceledQuantity > 0) {
    			long price = product.get("DISCOUNT_PRICE") == null ?
    					0 : new BigDecimal(product.get("DISCOUNT_PRICE").toString()).longValue();
    			long canceledAmount = canceledQuantity * price;
    			deliveryAmount -= canceledAmount;
    		}
    			
    	}
    	
    	
        //Response for order which uses COD method
        if (order.selectedPaymentMethods != null
        		&& order.selectedPaymentMethods.get("balances").toString().equals(PaymentOptionConstant.ID_CASH)) {
        	//Get User language
        	String language = header.get("language");
        	String paymentMethodName = PaymentOptionConstant.METHOD_NAME_CASH_VN;
        	if (language.equals(AppConstant.USER_LANG_EN))
        		paymentMethodName = PaymentOptionConstant.METHOD_NAME_CASH_EN;
        	else if (language.equals(AppConstant.USER_LANG_KR))
        		paymentMethodName = PaymentOptionConstant.METHOD_NAME_CASH_KR;
        	
        	orderDetailResponse = new OrderDetailResponse();
        	orderDetailResponse.setOrderId(order.orderId);
        	orderDetailResponse.setOrderStatus(convertOrderStatusResponse(order.status));
        	orderDetailResponse.setOrderValue(String.valueOf(order.amount.longValue()));
        	orderDetailResponse.setPaymentMethod(paymentMethodName);
        	orderDetailResponse.setOrderTime(orderTimeFormat.format(order.createdTime));
        	orderDetailResponse.setDeliveryInfo(deliveryInfo);
        	orderDetailResponse.setProductList(productList);
        	orderDetailResponse.setDeliveryAmount(String.valueOf(deliveryAmount));
    	}
        //Response for order which uses MegaV Payment method
        else {
        	orderDetailResponse = OrderPayByMegaVDetailResponse.builder()
        			.orderId(order.orderId)
        			.orderStatus(convertOrderStatusResponse(order.status))
        			.orderValue(String.valueOf(order.amount.longValue()))
        			.paymentMethod(PaymentOptionConstant.METHOD_NAME_MEGAV)
        			.orderTime(orderTimeFormat.format(order.createdTime))
        			.deliveryInfo(deliveryInfo)
        			.productList(productList)
        			.deliveryAmount(String.valueOf(deliveryAmount))
        			.totalDiscount(String.valueOf(order.discount.longValue()))
        			.paymentFee(String.valueOf(order.paymentAmount.subtract(order.totalAmount).longValue()))
        			.paymentAmount(String.valueOf(order.paymentAmount.longValue()))
        			.refundAmount(String.valueOf(order.refundAmount == null ? 0 : order.refundAmount.longValue()))
        			.build();
        }
        
        response.setData(orderDetailResponse);
        return response;
    }

    private String convertOrderStatusRequest(String orderStatus) {
        String result;
        switch (orderStatus) {
            case AppConstant.STT_TO_CONFIRM:
                result = AppConstant.ORDER_STT_PROCESSING;
                break;
            case AppConstant.STT_COMPLETED:
                result = AppConstant.ORDER_STT_RECEIVED;
                break;
            case AppConstant.STT_CANCELED:
                result = AppConstant.ORDER_STT_CANCELED;
                break;
            default:
                result = null;
        }
        return result;
    }
    
    private String convertOrderStatusResponse(String orderStatus) {
        String result;
        switch (orderStatus) {
            case AppConstant.ORDER_STT_PROCESSING:
                result = AppConstant.STT_TO_CONFIRM;
                break;
            case AppConstant.ORDER_STT_RECEIVED:
                result = AppConstant.STT_COMPLETED;
                break;
            case AppConstant.ORDER_STT_CANCELED:
                result = AppConstant.STT_CANCELED;
                break;
            default:
                result = null;
        }
        return result;
    }
    
    
    public CommonResponse<Object> selectDeliveryAddress(SelectDeliveryAddressRequest request, long userId, long userCompanyId,
    		String requestId) {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_SELECT_DELIVERY_ADDRESS;
    	
    	/**************** VALIDATION ****************/
    	//General validation
    	response = storeService.generalValidate(userCompanyId, DAESANG_STORE_ID, logCategory, requestId);
        if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
        	response.setData(null);
			return response;
		}
        
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
        		
		//Validate orderId
        if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
            logger.info("{} | {} | Order is not valid", logCategory, requestId);
            response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
            response.setData(null);
            return response;
        }
        
        //Get order info from DB and validate it
        Map<String, Object> order = iOrder.getDaesangOrderById(request.getOrderId(), DAESANG_STORE_ID);
        logger.info("{} | {} | OrderId={} | Get order from DB is done: {}", requestId, logCategory, request.getOrderId(), gson.toJson(order));
        if (order == null || order.isEmpty()) {
			response.setEcode(EcodeConstant.FAIL_ORDER_NOT_FOUND);
			response.setData(null);
			return response;
		}
        
//        if (Long.valueOf(order.get("USER_ID").toString()) != userId) {
//			logger.info("{} | {} | OrderId={} | Online User is not order owner", requestId, logCategory, request.getOrderId());
//			response.setEcode(EcodeConstant.FAIL_USER_NOT_ORDER_OWNER);
//        	response.setData(null);
//			return response;
//		}
        
        if (!String.valueOf(order.get("STATUS")).equals(AppConstant.ORDER_STT_NEW)) {
        	logger.info("{} | {} | OrderId={} | Cannot update delivery address for this order(order status is not NEW)", requestId, logCategory, request.getOrderId());
			response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
			response.setData(null);
			return response;
		}
        
        //Validate address information
        StoreFixedDeliveryAddress storeFixedDeliveryAddress = iStore.getDeliveryAddress(request.getAddressId(), DAESANG_STORE_ID);
        if (storeFixedDeliveryAddress == null) {
        	logger.info("{} | {} | OrderId={} | Get delivery address by Id={} from DB is null", requestId, logCategory, request.getOrderId(), request.getAddressId());
			response.setEcode(EcodeConstant.FAIL_INVALID_DELIVERY_ADDRESS);
			response.setData(null);
			return response;
		}
        
        if (!Utils.removeAccent(request.getName()).trim().equals(Utils.removeAccent(storeFixedDeliveryAddress.getName()).trim())
        		|| !Utils.removeAccent(request.getAddress()).trim().equals(Utils.removeAccent(storeFixedDeliveryAddress.getAddress()).trim())) {
        	logger.info("{} | {} | OrderId={} | Delivery address request is invalid", requestId, logCategory, request.getOrderId());
        	response.setEcode(EcodeConstant.FAIL_INVALID_DELIVERY_ADDRESS);
        	response.setData(null);
        	return response;
		}
        
        /************* PROCESS REQUEST **************/
        //1. Update delivery address, estimate delivery time and User info for the order
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, storeFixedDeliveryAddress.getFixedDeliveryTime());        
        Date etmDeliveryTime = calendar.getTime();
        
        iOrder.updateOrderDeliveryInfo(user.getPhoneNumber(), storeFixedDeliveryAddress.getAddress(), etmDeliveryTime, request.getOrderId());
        logger.info("{} | {} | OrderId={} | Updated info: UserPhone= {} | Delivery Address = {} | Estimate Delivery Time = {}",
        		requestId, logCategory, request.getOrderId(), user.getPhoneNumber(), storeFixedDeliveryAddress.getAddress(), etmDeliveryTime);
        
        //2. Prepare response data for client
        SelectedDeliveryAddressResponse selectedDeliveryAddressResponse = SelectedDeliveryAddressResponse.builder()
        		.username(username)
        		.phone(user.getPhoneNumber())
        		.deliveryAddress(storeFixedDeliveryAddress.getAddress())
        		.estimateDeliveryTime(new SimpleDateFormat(AppConstant.DELIVERY_TIME_FOMAT).format(etmDeliveryTime))
        		.build();
        
        response.setData(selectedDeliveryAddressResponse);
        
    	return response;
    }
    
    
    public CommonResponse<Object> createOrder(MobileBuyNowRequest request, long userId, long userCompanyId, String userPhone,
    		Map<String, String> header, String requestId) throws Exception {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_CREATE_ORDER;
    	String language = header.get("language");
    	
    	/**************** VALIDATION ****************/
    	
    	//Validate request storeId and check store status
    	if (request.getStoreId() == null) {
    		logger.info("{} | {} | StoreId in request is null", logCategory, requestId, request.getStoreId());
			response.setEcode(EcodeConstant.FAIL_INVALID_STORE_ID);
			return response;
		}
    	
    	response = storeService.generalValidate(userCompanyId, request.getStoreId().longValue(), logCategory, requestId);
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
			response.setData(null);
    		return response;
		}
    	Store store = (Store) response.getData();
    	response.setData(null);
    	
    	//Verify if the request productId is wrong
    	if (request.getProductId() == null || request.getProductId() < 0) {
    		logger.info("{} | {} | Request ProductId is invalid(null or less than 0)", logCategory, requestId);
			response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_FOUND);
			return response;
		}
    	
    	//Check product status and user buy threshold
    	Map<String, Object> productInfoAndBuyThreshold = iStoreProduct.getProductAndThreshold(store.getId(), request.getProductId());
    	response = checkPurchaseTime(request, userId, requestId, logCategory, productInfoAndBuyThreshold);
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
			return response;
		}
    	
    	//check the user's purchase threshold for classes 2 and 3 products during the month
    	if (Integer.valueOf(productInfoAndBuyThreshold.get("PRODUCT_CLASS").toString()) > 1) {
    		long productWeight = Long.valueOf(productInfoAndBuyThreshold.get("WEIGHT").toString());
    		long totalWeightPurchasing = request.getQuantity() * productWeight;
    		
	    	response = checkPurchaseLimitByProductWeight(userId, requestId, logCategory, totalWeightPurchasing);
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
    	    	
    	/************ CALL ORDER-SERIVCE ************/
    	
    	//Build order request
    	CreateOrderRequest createOrderRequest = buildCreateOrderRequest(request, userId, requestId, logCategory, store,
    			productInfoAndBuyThreshold, null);
    	
    	//Call createOrder API from order-service
    	CommonResponse<Object> createOrderResponse = getConnectOrderService(gson.toJson(createOrderRequest), CREATE_ORDER_API, header);
    	logger.info("{} | {} | Order-service response = {}", requestId, logCategory, gson.toJson(response));
    	
    	//Order-service response create order successful
    	if (createOrderResponse.getEcode().equals(EcodeConstant.SUCCESS)) {
    		response = handleSuccessfulResponseRromOrderService(request, productInfoAndBuyThreshold, createOrderRequest,
    				createOrderResponse, language, null);
    	} else {
    		response = createOrderResponse;
    	}
    	logger.info("{} | {} | Create order done | Response={}", requestId, logCategory, gson.toJson(response));
    	
    	return response;
    }
    
    
    public CommonResponse<Object> retryPlaceOrder(RetryPlaceOrderRequest request, long userId, long userCompanyId, String userPhone,
    		Map<String, String> header, String requestId) throws Exception {
    	CommonResponse<Object> response = new CommonResponse<>();
    	String logCategory = LogCategory.LOG_RETRY_PLACE_ORDER;
    	String language = header.get("language");
    	
    	/**************** VALIDATION ****************/
    	if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
    		logger.info("{} | {} | Request OrderID is null or empty", logCategory, requestId);
            response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
            return response;
    	}
    	
        Order order = getOrder(requestId, logCategory, request.getOrderId(), header);
        if (order == null) {
        	logger.info("{} | {} | Request orderId={} | Order not found", requestId, logCategory, request.getOrderId());
        	response.setEcode(EcodeConstant.FAIL_ORDER_NOT_FOUND);
            return response;
		}
        
        response = storeService.generalValidate(userCompanyId, order.storeId.longValue(), logCategory, requestId);
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
			response.setData(null);
    		return response;
		}
    	Store store = (Store) response.getData();
    	
        //Check if userId is valid
        if (order.userId == null || order.userId.longValue() != userId) {
            logger.info("{} | {} | User is not order owner or order.userId is null", logCategory, requestId);
            response.setData(null);
            response.setEcode(EcodeConstant.FAIL_USER_NOT_ORDER_OWNER);
            return response;
        }
        
        //Check order payment method selected: Fail if it is empty or COD method(it must be MegaV Welfare or Cash balance)
        if(order.selectedPaymentMethods == null || order.selectedPaymentMethods.isEmpty()
        		|| order.selectedPaymentMethods.get("balances").toString().equals(PaymentOptionConstant.ID_CASH)) {
        	logger.info("{} | {} | Order is not valid: selected payment method is empty", logCategory, requestId);
        	response.setData(null);
            response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
            return response;
        }
        
        //Check order status: only support retry place order which is FAIL and payment status not equal SUCCESS
        if (order.status == null || order.status.isEmpty()
        		|| !order.status.equals(AppConstant.ORDER_STT_FAIL)
        		|| !order.paymentStatus.equals(EcodeConstant.SUCCESS)) {
        	logger.info("{} | {} | Order status is not equal FAIL or order payment status is SUCCESS -> can't retry place order", logCategory, requestId);
        	response.setData(null);
        	response.setEcode(EcodeConstant.FAIL_ORDER_INVALID);
            return response;
		}
        
        //Get product status and user buy threshold from DB
    	Map<String, Object> productInfoAndBuyThreshold = iStoreProduct.getProductAndThresholdByProductCode(store.getId(), order.products.get(0).id);
    	
    	//Build a MobileCreateOrderRequest
    	MobileBuyNowRequest mobileCreateOrderRequest = MobileBuyNowRequest.builder()
    			.storeId(store.getId())
    			.productId(Long.valueOf(String.valueOf(productInfoAndBuyThreshold.get("PRODUCT_ID"))))
    			.quantity(order.products.get(0).quantity)
    			.build();
    	
    	response = checkPurchaseTime(mobileCreateOrderRequest, userId, requestId, logCategory, productInfoAndBuyThreshold);
    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
			return response;
		}
    	
    	//check the user's purchase threshold for classes 2 and 3 products during the month
    	if (Integer.valueOf(productInfoAndBuyThreshold.get("PRODUCT_CLASS").toString()) > 1) {
    		long productWeight = Long.valueOf(productInfoAndBuyThreshold.get("WEIGHT").toString());
    		long totalWeightPurchasing = mobileCreateOrderRequest.getQuantity() * productWeight;
    		
	    	response = checkPurchaseLimitByProductWeight(userId, requestId, logCategory, totalWeightPurchasing);
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
        
    	/************ CALL ORDER-SERIVCE ************/
    	//Build order request
    	CreateOrderRequest createOrderRequest = buildCreateOrderRequest(mobileCreateOrderRequest, userId, requestId, logCategory, store,
    			productInfoAndBuyThreshold, null);
    	
    	//Call createOrder API from order-service
    	CommonResponse<Object> createOrderResponse = getConnectOrderService(gson.toJson(createOrderRequest), CREATE_ORDER_API, header);
    	logger.info("{} | {} | Order-service response = {}", requestId, logCategory, gson.toJson(response));
    	
    	//Order-service response create order successful
    	if (createOrderResponse.getEcode().equals(EcodeConstant.SUCCESS)) {
    		response = handleSuccessfulResponseRromOrderService(mobileCreateOrderRequest, productInfoAndBuyThreshold,
					createOrderRequest, createOrderResponse, language, null);
    	} else {
    		response = createOrderResponse;
    	}
    	logger.info("{} | {} | Retry create order done | Response={}", requestId, logCategory, gson.toJson(response));
    	
    	return response;
        
	}
    
    
    /**
	 * @param request
	 * @param userId
	 * @param requestId
	 * @param response
	 * @param logCategory
	 * @param store
	 * @param productInfoAndBuyThreshold
	 * @throws NumberFormatException
	 */
    public CommonResponse<Object> checkPurchaseTime(MobileBuyNowRequest request, long userId, String requestId,
			String logCategory, Map<String, Object> productInfoAndBuyThreshold) throws NumberFormatException {
		CommonResponse<Object> response = new CommonResponse<>();
		
		if (request != null) {
			if(productInfoAndBuyThreshold == null || productInfoAndBuyThreshold.isEmpty()) {
	    		logger.info("{} | {} | Product not found by productId = {}", logCategory, requestId, request.getProductId());
	    		response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_FOUND);
	    		return response;
	    	} else if(Integer.valueOf(productInfoAndBuyThreshold.get("PRODUCT_STATUS").toString()) != AppConstant.PRODUCT_STATUS_ACTIVE) {
	    		logger.info("{} | {} | The product has stopped selling", logCategory, requestId);
	    		response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_ACTIVE);
	    		return response;
			} else if (productInfoAndBuyThreshold.get("PRODUCT_CODE") == null || productInfoAndBuyThreshold.get("PRODUCT_CODE").toString().isEmpty()) {
				logger.info("{} | {} | This product has an invalid code(PRODUCT_CODE)", logCategory, requestId);
	    		response.setEcode(EcodeConstant.FAIL_PRODUCT_NOT_ACTIVE);
	    		return response;
			}
		}
    	if (productInfoAndBuyThreshold.get("SOT_STATUS") != null && !productInfoAndBuyThreshold.get("SOT_STATUS").toString().isEmpty()) {
    		
    		//Check if this product is only bought on the day of the week
    		if (productInfoAndBuyThreshold.get("BUY_ON_DAY_OF_WEEK") != null && !productInfoAndBuyThreshold.get("BUY_ON_DAY_OF_WEEK").toString().isEmpty()) {
				List<BuyOnDayOfWeekConfiguration> listOfDatesCanPurchase = gson.fromJson(productInfoAndBuyThreshold.get("BUY_ON_DAY_OF_WEEK").toString(),
						new TypeToken<List<BuyOnDayOfWeekConfiguration>>() {}.getType());
				Calendar calendar = Calendar.getInstance();
				int today = calendar.get(Calendar.DAY_OF_WEEK);
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				
				BuyOnDayOfWeekConfiguration todayCanPurchase = null;
				for (BuyOnDayOfWeekConfiguration dateCanPurchase : listOfDatesCanPurchase) {
					if (dateCanPurchase.getDay() == today) {
						todayCanPurchase = dateCanPurchase;
						break;
					}
				}
				
				if (todayCanPurchase == null || hour < todayCanPurchase.getStartHour() || hour > todayCanPurchase.getEndHour()) {
					logger.info("{} | {} | Cannot buy this product today(Buy On Day Of Week)", logCategory, requestId);
		    		response.setEcode(EcodeConstant.FAIL_BUY_ON_SPECIAL_DAY);
		    		return response;
				}
			}
    		
    		//Check if this product is only bought on the day of the month
    		if (productInfoAndBuyThreshold.get("BUY_ON_DAY_OF_MONTH") != null && !productInfoAndBuyThreshold.get("BUY_ON_DAY_OF_MONTH").toString().isEmpty()) {
				String[] listOfDatesCanPurchase = Utils.convertStringToArray(productInfoAndBuyThreshold.get("BUY_ON_DAY_OF_MONTH").toString(), ',');
				String today = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
				boolean canBuyToday = Arrays.asList(listOfDatesCanPurchase).contains(today);
				if (!canBuyToday) {
					logger.info("{} | {} | Cannot buy this product today(Buy On Day Of Month)", logCategory, requestId);
		    		response.setEcode(EcodeConstant.FAIL_BUY_ON_SPECIAL_DAY);
		    		return response;
				}
			}
		}
    	
    	response.setEcode(EcodeConstant.SUCCESS);
    	return response;
	}
	
	
	/**
	 * @param userId
	 * @param requestId
	 * @param logCategory
	 * @param totalWeightPurchasing: total weight of all products(in classes 2 and 3) by order request
	 * @return
	 */
    public CommonResponse<Object> checkPurchaseLimitByProductWeight(long userId, String requestId, String logCategory,
			long totalWeightPurchasing) {
		CommonResponse<Object> response = new CommonResponse<>();
		
		if (PURCHASE_LIMIT_BY_WEIGHT >= 0) {
			Map<String, Object> totalVolumeUserBoughtInMonth = iOrder.getTotalVolumeUserBoughtInThisMonth(DAESANG_STORE_ID, userId);
			
			if (totalVolumeUserBoughtInMonth != null && !totalVolumeUserBoughtInMonth.isEmpty()) {
				long totalWeightOrdered = totalVolumeUserBoughtInMonth.get("ORDERED") == null ?
						0 : new BigDecimal(totalVolumeUserBoughtInMonth.get("ORDERED").toString()).longValue();
				long totalWeightCanceled = totalVolumeUserBoughtInMonth.get("CANCELED") == null ?
						0 : new BigDecimal(totalVolumeUserBoughtInMonth.get("CANCELED").toString()).longValue();
				long totalWeightBought = totalWeightOrdered - totalWeightCanceled;
				
				if (PURCHASE_LIMIT_BY_WEIGHT < (totalWeightBought + totalWeightPurchasing)) {
					logger.info("{} | {} | Order request is violated max weight per month", requestId, logCategory);
					response.setEcode(EcodeConstant.FAIL_MAX_WEIGHT_PER_MONTH);
					Double maxWeight =  (double) PURCHASE_LIMIT_BY_WEIGHT;
			    	Double bought = (double) totalWeightBought;
			    	Double remain = ((maxWeight - bought) / 1000);
					response.setData(remain);//The remaining weight can be purchased in kilograms
					return response;
				}
			} else if (PURCHASE_LIMIT_BY_WEIGHT < totalWeightPurchasing) {
				logger.info("{} | {} | Order request is violated max weight per month", requestId, logCategory);
				response.setEcode(EcodeConstant.FAIL_MAX_WEIGHT_PER_MONTH);
				response.setData(PURCHASE_LIMIT_BY_WEIGHT/1000);
				return response;
			}
		}
		
		response.setEcode(EcodeConstant.SUCCESS);
		return response;
	}
	

	/**
	 * @param request
	 * @param userId
	 * @param requestId
	 * @param logCategory
	 * @param store
	 * @param productInfoAndBuyThreshold
	 * @return CreateOrderRequest (request is used to send to order-service to create a new order)
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	protected CreateOrderRequest buildCreateOrderRequest(MobileBuyNowRequest request, long userId, String requestId,
			String logCategory, Store store, Map<String, Object> productInfoAndBuyThreshold,
			List<Map<String, Object>> cartProductData) throws NumberFormatException, Exception {
		
    	List<Map<String, String>> listOfStorePaymentMethods = iStore.getStorePaymentMethod(EPAY_MERCHANT_GROUP_ID, DAESANG_SERVICE_ID);
    	if (listOfStorePaymentMethods == null || listOfStorePaymentMethods.isEmpty()) {
			logger.info("{} | {} | List of store's payment method is null or empty", requestId, logCategory);
			throw new Exception("No matching payment method found");
		}
    	
    	List<String> paymentMethods = new ArrayList<>();
    	for (Map<String, String> method : listOfStorePaymentMethods) {
			paymentMethods.add(method.get("PAYMENT_METHOD_ID").toString());
		}
    	
    	User user = iUser.getUserById(userId);
    	if (user == null) {
			logger.info("{} | {} | Get User info from DB is null", requestId, logCategory);
			throw new Exception("User not found");
		}
    	
    	List<OrderProductRequest> listOrderProduct = new ArrayList<>();
    	BigDecimal orderAmount = null;
    	BigDecimal totalAmount = null;
    	if (request != null) {
    		long productAmount = Long.valueOf(productInfoAndBuyThreshold.get("DISCOUNT_PRICE").toString());
    		OrderProductRequest product = OrderProductRequest.builder()
        			.id(productInfoAndBuyThreshold.get("PRODUCT_CODE").toString())
        			.name(productInfoAndBuyThreshold.get("PRODUCT_NAME").toString())
        			.category("")
        			.quantity(request.getQuantity())
        			.price(productAmount)
        			.totalAmount(productAmount * request.getQuantity())
        			.build();
    		listOrderProduct.add(product);
    		orderAmount = new BigDecimal(productAmount * request.getQuantity());
    		totalAmount = new BigDecimal(productAmount * request.getQuantity());
		} else if (cartProductData != null && !cartProductData.isEmpty()) {
			orderAmount = new BigDecimal(0);
    		totalAmount = new BigDecimal(0);
    		
			for (Map<String, Object> productData : cartProductData) {
				long productAmount = Long.valueOf(productData.get("DISCOUNT_PRICE").toString());
				int quantity = Integer.valueOf(productData.get("QUANTITY").toString());
				OrderProductRequest product = OrderProductRequest.builder()
	        			.id(productData.get("PRODUCT_CODE").toString())
	        			.name(productData.get("NAME").toString())
	        			.category("")
	        			.quantity(quantity)
	        			.price(productAmount)
	        			.totalAmount(productAmount * quantity)
	        			.build();
	    		listOrderProduct.add(product);
	    		orderAmount = orderAmount.add(BigDecimal.valueOf(product.getTotalAmount()));
	    		totalAmount = totalAmount.add(BigDecimal.valueOf(product.getTotalAmount()));
			}
		} else {
			throw new Exception("No product data found to create order!!!!");
		}
    	
    	Receiver receiver = Receiver.builder()
    			.name(user.getName() == null ? "" : user.getName())
    			.phone(user.getPhoneNumber())
    			.email(user.getEmail() == null ? "" : user.getEmail())
    			.build();
    	
    	CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
    			.merchantId(EPAY_MERCHANT_GROUP_ID)
    			.serviceId(DAESANG_SERVICE_ID)
    			.amount(orderAmount)
    			.totalAmount(totalAmount)
    			.storeId(store.getId())
    			.paymentMethods(paymentMethods)
    			.receiver(receiver)
    			.products(listOrderProduct)
    			.build();
    	logger.info("{} | {} | CreateOrderRequest(to Order-service) = {}", requestId, logCategory, gson.toJson(createOrderRequest));
		return createOrderRequest;
	}
    
    
	/**
	 * @param request
	 * @param response
	 * @param productInfoAndBuyThreshold
	 * @param createOrderRequest
	 * @param language
	 * @throws JsonSyntaxException
	 * @throws NumberFormatException
	 */
	protected CommonResponse<Object> handleSuccessfulResponseRromOrderService(MobileBuyNowRequest request, Map<String, Object> productInfoAndBuyThreshold,
			CreateOrderRequest createOrderRequest, CommonResponse<Object> createOrderResponse, String language,
			List<Map<String, Object>> cartProductCheckoutData) throws JsonSyntaxException, NumberFormatException {
		CommonResponse<Object> response = new CommonResponse<>();
		List<OrderProductResponse> listOrderProductResposne = new ArrayList<>();
		long originalAmount = 0;
		
		//Product response info
		if (request != null) {
			// case này bấm byNow từ màn chi tiết sản phẩm
			OrderProductResponse orderProductResponse = OrderProductResponse.builder()
					.productCode(String.valueOf(productInfoAndBuyThreshold.get("PRODUCT_CODE")))
					.name(String.valueOf(productInfoAndBuyThreshold.get("PRODUCT_NAME")))
					.image(String.valueOf(productInfoAndBuyThreshold.get("IMAGE")))
					.price(String.valueOf(productInfoAndBuyThreshold.get("DISCOUNT_PRICE")))
					.originalPrice(String.valueOf(productInfoAndBuyThreshold.get("PRICE")))
					.quantity(request.getQuantity())
					.build();
			listOrderProductResposne.add(orderProductResponse);
			originalAmount += Long.valueOf(productInfoAndBuyThreshold.get("PRICE").toString()) * request.getQuantity();
		} else if (cartProductCheckoutData != null && !cartProductCheckoutData.isEmpty()) {
			// case này bấm mua từ màn cart
			for (Map<String, Object> productData : cartProductCheckoutData) {
				OrderProductResponse orderProductResponse = OrderProductResponse.builder()
						.productCode(String.valueOf(productData.get("PRODUCT_CODE")))
						.name(String.valueOf(productData.get("PRODUCT_NAME")))
						.image(String.valueOf(productData.get("IMAGE")))
						.price(String.valueOf(productData.get("DISCOUNT_PRICE")))
						.originalPrice(String.valueOf(productData.get("PRICE")))
						.quantity(Integer.valueOf(productData.get("QUANTITY").toString()))
						.build();
				listOrderProductResposne.add(orderProductResponse);
				originalAmount += (Long.valueOf(orderProductResponse.getOriginalPrice()) * orderProductResponse.getQuantity());
			}
		}
		
		//Order amount response
		CreateOrderResponseData createOrderResponseData = gson.fromJson(gson.toJson(createOrderResponse.getData()), CreateOrderResponseData.class);
		String paymentAmount = String.valueOf(createOrderResponseData.getAmount().longValue());
		
		//List of payment option response
		Map<String, String> megavOption = new HashMap<>();
		megavOption.put("id", PaymentOptionConstant.ID_MEGAV);
		megavOption.put("name", PaymentOptionConstant.METHOD_NAME_MEGAV);
		megavOption.put("moneySource", createOrderRequest.getReceiver().getPhone());
		megavOption.put("icon", ICON_PAYMENT_MEGAV);
		
		/**
		 * Bỏ phương thức thanh toán này theo yêu cầu mới nhất của Daesang
		 */
//		Map<String, String> cashOption = new HashMap<>();
//		cashOption.put("id", PaymentOptionConstant.ID_CASH);
//		cashOption.put("icon", ICON_PAYMENT_CASH);
//		switch (language) {
//		case AppConstant.USER_LANG_VN:
//			cashOption.put("moneySource", PaymentOptionConstant.CASH_MONEY_SOURCE_TEXT_VN);
//			cashOption.put("name", PaymentOptionConstant.METHOD_NAME_CASH_VN);
//			break;
//		case AppConstant.USER_LANG_EN:
//			cashOption.put("moneySource", PaymentOptionConstant.CASH_MONEY_SOURCE_TEXT_EN);
//			cashOption.put("name", PaymentOptionConstant.METHOD_NAME_CASH_EN);
//			break;
//		case AppConstant.USER_LANG_KR:
//			cashOption.put("moneySource", PaymentOptionConstant.CASH_MONEY_SOURCE_TEXT_KR);
//			cashOption.put("name", PaymentOptionConstant.METHOD_NAME_CASH_KR);
//			break;
//		default:
//			cashOption.put("moneySource", PaymentOptionConstant.CASH_MONEY_SOURCE_TEXT_EN);
//			cashOption.put("name", PaymentOptionConstant.METHOD_NAME_CASH_EN);
//			break;
//		}
		
		//Add all to data response
		Map<String, Object> data = new HashMap<>();
		data.put("orderId", createOrderResponseData.getOrderId());
		data.put("products", listOrderProductResposne);
		data.put("paymentAmount", paymentAmount);
		data.put("originalAmount", String.valueOf(originalAmount));
//		data.put("paymentOptions", Arrays.asList(megavOption, cashOption));
		response.setData(data);
		response.setEcode(EcodeConstant.SUCCESS);
		
		return response;
	}
	
    
    private Order getOrder(String requestId, String logCategory, String orderId, Map<String, String> header) {
		HashMap<String, Object> request = new HashMap<>();
		request.put("orderId", orderId);

		String jsonRequest = gson.toJson(request);
		CommonResponse<Object> response = getConnectOrderService(jsonRequest, GET_ORDER_API, header);
		
		Order order = gson.fromJson(gson.toJson(response.getData()), Order.class);

		logger.info("{} | {} | Get order done | orderId={} | order={}",
				requestId, logCategory, orderId, gson.toJson(order));

		return order;
	}
    
    @SuppressWarnings("unchecked")
	protected CommonResponse<Object> getConnectOrderService(String jsonObject, String endPoint, Map<String, String> header) {
		CommonResponse<Object> res = new CommonResponse<Object>();
		logger.info("{} | Connection order Service start | jsonObject={} | endPoint={}  ", header.get("requestid"), jsonObject,
				endPoint);
		CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
				.build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);

		Gson gson = new Gson();
		try {
			HttpPost request = new HttpPost(endPoint);
			request.setHeader("Content-Type", "application/json");
			request.addHeader("Authorization", header.get("authorization"));
			request.addHeader("requestId", header.get("requestid"));
			request.addHeader("device", header.get("device"));
			request.addHeader("language", header.get("language"));
			request.setEntity(new StringEntity(jsonObject, "UTF-8"));
			HttpResponse response = httpClient.execute(request);

			int status = response.getStatusLine().getStatusCode();
			HttpEntity entityApi = response.getEntity();
			String rs = EntityUtils.toString(entityApi, "UTF-8");
			if (status == HttpStatus.SC_OK) {

				res = gson.fromJson(rs, CommonResponse.class);
				logger.info("Response Get Order=={}", rs);
			} else {
				logger.info("{} | ConnectionService | status={} | body={} ", header.get("requestid"), status, rs);
				res.setData(EcodeConstant.PENDING);
				res.setEcode(EcodeConstant.PENDING);
			}
		} catch (Exception e) {
			logger.fatal("{} | Connection order Service | error={}", header.get("requestid"), e);
			res.setEcode(EcodeConstant.ERROR);
			res.setMessage(e.getMessage());
		}
		return res;
	}

    
    /**
     * This function is used only for internal service(payment-service)
     * Payment service will call this function when the client requests to confirm-payment-service
     * @param order
     * @param requestId
     * @param language
     * @return CommonResponse<Object>
     * @throws Exception
     */
    public CommonResponse<Object> verifyOrder(Order order, String requestId, String language) {
    	CommonResponse<Object> response = new CommonResponse<Object>();
    	String logCategory = LogCategory.LOG_VERIFY_ORDER;
    	long userId = order.userId;
    	List<String> listCartProductId = new ArrayList<>();
    	long totalWeightPurchasing = 0;
    	
    	try {
        	//Case checkout cart
    		List<String> productCode = new ArrayList<>();
        	for (Order.Product product : order.products) {
    			productCode.add(product.id);
    		}
        	
    		//Load the product's purchase threshold info from the database 
    		List<Map<String, Object>> listProductThreshHold = iDucVietCart.loadListCartProductCheckout(userId, productCode);
        		
    		if (listProductThreshHold != null && !listProductThreshHold.isEmpty()) {
        		//1. Check purchase time
            	for (Map<String, Object> productThreshold : listProductThreshHold) {
            		response = checkPurchaseTime(null, userId, requestId, logCategory, productThreshold);
                	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
                		logger.info("{} | {} | Response check purchase time: {}", requestId, logCategory, gson.toJson(response));
            			return response;
            		}
                	
                	//Prepare data for next step
                	listCartProductId.add(productThreshold.get("CP_ID").toString());
                	
                	//Getting total weight of list of products are purchasing from cart checkout which is class 2 or 3
                	if (Integer.valueOf(productThreshold.get("PRODUCT_CLASS").toString()) > 1) {
            			long productWeight = Long.valueOf(productThreshold.get("WEIGHT").toString());
            			int quantity = Integer.valueOf(productThreshold.get("QUANTITY").toString());
        				totalWeightPurchasing += (productWeight * quantity);
        			}
        		}
        	} 
        	//Case buy now
        	else {
        		//Load the product's purchase threshold info from the database
        		Map<String, Object> productInfoAndBuyThreshold = iStoreProduct.getProductAndThresholdByProductCode(order.storeId, order.products.get(0).id);
        		
        		response = checkPurchaseTime(null, userId, requestId, logCategory, productInfoAndBuyThreshold);
        		logger.info("{} | {} | Response check purchase time: {}", requestId, logCategory, gson.toJson(response));
            	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
        			return response;
        		}
            	
        		//check the user's purchase threshold for classes 2 and 3 products during the month
            	if (Integer.valueOf(productInfoAndBuyThreshold.get("PRODUCT_CLASS").toString()) > 1) {
            		long productWeight = Long.valueOf(productInfoAndBuyThreshold.get("WEIGHT").toString());
            		totalWeightPurchasing = order.products.get(0).quantity * productWeight;
            	}
        		
    		}
        	
        	//2. Check the user's purchase threshold for classes 2 and 3 products during the month
        	if (totalWeightPurchasing > 0) {
    	    	response = checkPurchaseLimitByProductWeight(userId, requestId, logCategory, totalWeightPurchasing);
    	    	logger.info("{} | {} | Response check purchase limit by product weight: {}", requestId, logCategory, gson.toJson(response));
    	    	if (!response.getEcode().equals(EcodeConstant.SUCCESS)) {
    	    		Ecode ecode = codeService.getEcode(response.getEcode(), language);
    				response.setMessage(ecode.getMessage().replace("?", response.getData().toString()));
    				response.setP_ecode(ecode.getP_ecode());
    				response.setP_message(ecode.getP_message());
    				response.setData(null);
    			}
        	}
        	
        	return response;
		} catch (Exception e) {
			logger.fatal("{} | {} | Exception: ", requestId, logCategory, e);
			response.setEcode(EcodeConstant.ERROR);
			return response;
		}
    	//Always delete products from the shopping cart after checkout whether the order was successful or not
    	finally {
    		if (listCartProductId != null && !listCartProductId.isEmpty() && response.getEcode().equals(EcodeConstant.SUCCESS)) {
    			logger.info("{} | {} | Start delete products from the shopping cart after checkout .....", requestId, logCategory);
    			iDucVietCart.removeProductsFromCart(listCartProductId);
    			logger.info("{} | {} | Delete products from the shopping cart is done .....", requestId, logCategory);
			}
		}
    	
    }
}
