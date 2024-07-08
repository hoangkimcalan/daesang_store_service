package com.epay.ewallet.store.daesang.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreFixedDeliveryAddressResponse {
	private long id;
    private String name;
    private String address;
}
