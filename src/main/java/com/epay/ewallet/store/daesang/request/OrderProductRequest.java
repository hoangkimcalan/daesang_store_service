package com.epay.ewallet.store.daesang.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderProductRequest {
	
	private String id;
	private String name;
	private String category;
	private int quantity;
	private long price;
	private long totalAmount;
	
}
