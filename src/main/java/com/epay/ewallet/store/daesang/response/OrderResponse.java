package com.epay.ewallet.store.daesang.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    public String orderId;
    public String status;
    private String productImage;
    public String productName;
    public String orderAmount;
    public Integer totalProduct;
}
