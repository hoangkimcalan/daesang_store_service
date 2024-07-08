package com.epay.ewallet.store.daesang.response;

import lombok.Data;

import java.util.List;

@Data
public class VisitStoreResponse {
    private long storeId;
    private String storeName;
    private List<StoreBannerResponse> banners;
    private long currentTotalCartItem;
}
