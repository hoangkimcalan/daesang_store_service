package com.epay.ewallet.store.daesang.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreBannerResponse {
	private long id;
    private String image;
    private int priority;
}
