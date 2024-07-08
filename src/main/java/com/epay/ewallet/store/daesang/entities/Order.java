package com.epay.ewallet.store.daesang.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Order {

	public String orderId;
	public String merchantOrderId;

	public Service service;
	public Merchant merchant;
	
	public BigDecimal amount;
	public BigDecimal totalAmount;
	public BigDecimal paymentAmount;
	public BigDecimal refundAmount;
	public BigDecimal discount;
	public BigDecimal cashBack;

	public String status;
	public String paymentStatus;
	public String productStatus;
	public String cashBackStatus;
	public Date createdTime;
	public Long userId;
	public Long storeId;
	public String estimateDeliveryTime;
	public String deliveryAddress;

	public ArrayList<String> paymentMethods;
//	public ArrayList<String> suggestedPaymentMethods;
	public Map<String, Object> selectedPaymentMethods;

	public Receiver receiver;

	public ArrayList<Product> products;

	public static class Service {
		public String id;
		public String name;
	}

	public static class Merchant {
		public String id;
		public String name;
	}

	public static class Receiver {
		public String name;
		public String address;
		public String email;
		public String phone;
	}

	public static class Product {
		public String id;
		public String name;
		public String category;
		public int quantity;
		public BigDecimal price;
		public BigDecimal totalAmount;
		public int canceledQuantity;
	}

}
