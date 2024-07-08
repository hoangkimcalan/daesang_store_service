package com.epay.ewallet.store.daesang.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryAddressResponse {
    private long storeId;
    private String storeName;
    private String username;
    private String phone;
    private List<StoreFixedDeliveryAddressResponse> deliveryAddress;
}
