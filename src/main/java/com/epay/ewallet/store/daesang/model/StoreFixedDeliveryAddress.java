package com.epay.ewallet.store.daesang.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TBL_STORE_FIXED_DLV_ADDR
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreFixedDeliveryAddress {
    
	private long id;
	private long storeId;
	private int status;
    private String name;
    private String address;
    private String ward;
    private String district;
    private String city;
    private Date createdAt;
    private Date updatedAt;
    private int fixedDeliveryTime; //total days of delivery time 
    
}
