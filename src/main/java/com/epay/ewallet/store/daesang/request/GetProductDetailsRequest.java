package com.epay.ewallet.store.daesang.request;

import lombok.Data;

@Data
public class GetProductDetailsRequest {
    private long storeId;
    private long productId;
}
