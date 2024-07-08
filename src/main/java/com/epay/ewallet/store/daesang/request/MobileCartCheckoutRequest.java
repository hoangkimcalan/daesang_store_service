package com.epay.ewallet.store.daesang.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobileCartCheckoutRequest {
	
	private long storeId;
	private List<CartProduct> products;
	
	public static class CartProduct {
		public String productCode;
		public int quantity;
	}
	
}
