package com.epay.ewallet.store.daesang.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OrderPayByMegaVDetailResponse extends OrderDetailResponse {

	private String totalDiscount;
	private String paymentFee;
	private String paymentAmount;
	private String refundAmount;

	@Builder
	public OrderPayByMegaVDetailResponse(String orderId, String orderStatus, Map<String, String> deliveryInfo,
			List<OrderProductResponse> productList, String orderValue, String paymentMethod, String orderTime,
			String deliveryAmount, String totalDiscount, String paymentFee, String paymentAmount, String refundAmount) {
		super(orderId, orderStatus, deliveryInfo, productList, orderValue, paymentMethod, orderTime, deliveryAmount);
		this.totalDiscount = totalDiscount;
		this.paymentFee = paymentFee;
		this.paymentAmount = paymentAmount;
		this.refundAmount = refundAmount;
	}

}
