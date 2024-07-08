package com.epay.ewallet.store.daesang.response;

import com.epay.ewallet.store.daesang.request.OrderProductRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderProductResponse {
	
	private String productCode;
	private String name;
	private String image;
	private String price;
	private String originalPrice;
	private int quantity;
	private Integer deliveryQuantity; //only use for partial delivery order, api getOrderDetail.
	
}
