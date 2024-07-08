package com.epay.ewallet.store.daesang.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchProductsRequest {
    private long storeId;
    private String productName;
    private int page;
}
