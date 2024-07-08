package com.epay.ewallet.store.daesang.model;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DucVietCart {
	
	private String id; //id = userId + productId
	private BigDecimal userId;
	private String productId;
	private long quantity;
	private int status;
	private Date createdAt;
	private Date updatedAt;
	
}
