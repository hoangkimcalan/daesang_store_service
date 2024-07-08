package com.epay.ewallet.store.daesang.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectDeliveryAddressRequest {
	
	private String orderId;
	private long addressId;
	private String name;
	private String address;
	
}