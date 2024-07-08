package com.epay.ewallet.store.daesang.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TBL_STORE_PRODUCT Join TBL_STORE_PRODUCT_IMG 
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetail {
    private long id;
    private long storeId;
    private int status;
    private int priority;
    private String link;
    private String name;
    private long originPrice;
    private long discountPrice;
    private long discountPercent;
    private String productDetails;
}
