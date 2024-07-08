package com.epay.ewallet.store.daesang.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreProductResponse {
	
	private long id;
    private String image;
    private String name;
    private String originPrice;
    private String discountPrice;
    private String discountPercent;
    
}
