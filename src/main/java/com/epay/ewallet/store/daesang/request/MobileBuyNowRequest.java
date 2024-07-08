package com.epay.ewallet.store.daesang.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileBuyNowRequest {
	
	private Long storeId;
	private Long productId;
	private Integer quantity;
	
}
