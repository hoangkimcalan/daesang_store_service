package com.epay.ewallet.store.daesang.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderRequest {
	private String merchantId;
	private String serviceId;
	private String subMerchantId;
	private String merchantCategoryCode;
	private String referenceOrderId;
	private BigDecimal amount;
	private BigDecimal totalAmount;
	private Receiver receiver;
	private String message;
	private List<OrderProductRequest> products;
	private List<String> paymentMethods;
	private long storeId;
	private String estimateDeliveryTime; //yyyy-MM-dd
    
}
