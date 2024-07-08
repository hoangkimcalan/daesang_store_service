package com.epay.ewallet.store.daesang.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SelectedDeliveryAddressResponse {
	
	private String username;
	private String phone;
	private String deliveryAddress;
	private String estimateDeliveryTime; //(dd/MM/yyyy)
	
}
