package com.epay.ewallet.store.daesang.constant;

public class AppConstant {

    public static final String USER_LANG_VN = "VN";
    public static final String USER_LANG_EN = "EN";
    public static final String USER_LANG_KR = "KR";
    
    public static final int USER_CART_LIMITATION = 200;
    public static final int PRODUCTS_PER_PAGE = 20;

    public static final long DAESANG_COMPANY_ID = 3;
    public static final long DAESANG_STORE_ID = 4;
    public static final int COMPANY_STATUS_ACTIVE = 1;
    public static final int STORE_STATUS_ACTIVE = 1;
    public static final int PRODUCT_STATUS_ACTIVE = 1;
    public static final int PRODUCT_STATUS_INACTIVE = 0;
    
    //Order status
    public static final String ORDER_STT_NEW = "NEW";
    public static final String ORDER_STT_FAIL = "FAIL";
    public static final String ORDER_STT_PROCESSING = "PROCESSING";
//    public static final String ORDER_STT_SHIPPING = "SHIPPING";
    public static final String ORDER_STT_CANCELED = "CANCELED";
    public static final String ORDER_STT_RECEIVED = "RECEIVED";
    
    //Status support filter user's orders
    public static final String STT_TO_CONFIRM = "TO_CONFIRM";
//    public static final String STT_TO_RECEIVE = "TO_RECEIVE";
    public static final String STT_CANCELED = "CANCELED";
    public static final String STT_COMPLETED = "COMPLETED";
    
    //Daesang delivery time format
    public static final String DELIVERY_TIME_FOMAT = "dd/MM/yyyy";
    public static final String ORDER_CREATED_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    
    public static final String CART_CHOICE_ALL_PRODUCT = "ALL";
    public static final String CART_CHOICE_PARTIAL_PRODUCT = "PART";
    
}
