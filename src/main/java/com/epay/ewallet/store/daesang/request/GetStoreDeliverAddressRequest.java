package com.epay.ewallet.store.daesang.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetStoreDeliverAddressRequest {
    private long storeId;
}