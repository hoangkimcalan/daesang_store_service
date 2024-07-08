package com.epay.ewallet.store.daesang.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetProductsDetailsResponse {
    private Long id;
    private List<ProductImages> images;
    private String name;
    private String originPrice;
    private String discountPrice;
    private String discountPercent;
    private String productDetails;
}
