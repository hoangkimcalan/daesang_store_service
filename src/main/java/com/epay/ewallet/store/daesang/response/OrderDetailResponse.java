package com.epay.ewallet.store.daesang.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderDetailResponse {
    private String orderId;
    private String orderStatus;
    private Map<String, String> deliveryInfo; //structure of this field: {username, phone, address, deliveryTime}
    private List<OrderProductResponse> productList;
    private String orderValue;
    private String paymentMethod;
    private String orderTime;
    private String deliveryAmount; //set value for this field when the order has the status of shipping or completed

}
