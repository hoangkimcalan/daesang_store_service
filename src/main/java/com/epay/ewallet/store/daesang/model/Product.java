package com.epay.ewallet.store.daesang.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TBL_STORE_PRODUCT Join TBL_STORE_PRODUCT_IMG 
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private long id;
    private String image;
    private String name;
    private long originPrice;
    private long discountPrice;
    private long discountPercent;
}
