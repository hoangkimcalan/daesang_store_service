package com.epay.ewallet.store.daesang.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data is returned in response from API CreateOrder of Order-service
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponseData {
	
	private String merchantId;
	private String serviceId;
	private String orderId;
	private String referenceOrderId;
	private BigDecimal amount;
	
}
