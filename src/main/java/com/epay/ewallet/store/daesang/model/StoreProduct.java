package com.epay.ewallet.store.daesang.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TBL_STORE_PRODUCT
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class StoreProduct {
	
	private long id;
	private long storeId;
	private long categoryId;
	private String name;
	private long price;
	private long weight;
	private String productInfo;
	private int status;
	private String brand;
	private long discountPrice;
	private int discountPercent;
	private Date createdAt;
	private Date updatedAt;
	
}
