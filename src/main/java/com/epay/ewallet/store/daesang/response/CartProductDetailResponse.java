package com.epay.ewallet.store.daesang.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartProductDetailResponse {
	
	private String productCode; 
	private String name;
	private String image;
	private String price;
	private String originalPrice;
	private int quantity;
	private String message;
	
}
