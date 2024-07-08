package com.epay.ewallet.store.daesang.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCartProductQuantityRequest {
	
	private String productCode;
	private int newQuantity;
	
}
