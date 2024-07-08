package com.epay.ewallet.store.daesang.request;

import lombok.Data;

@Data
public class GetAllOrderRequest {
    private long storeId;
    private String orderStatus;
    private int page;

}

