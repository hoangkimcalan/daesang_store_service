package com.epay.ewallet.store.daesang.mapperOne;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IOrder {
	List<Map<String, Object>> getOrders(@Param("storeId") long storeId, @Param("serviceId") String serviceId,
			@Param("orderStatus") String orderStatus, @Param("userId") long userId, @Param("start") int start,
			@Param("end") int end);

	//Using for ole query
//	Map<String, Object> getTotalVolumeUserBoughtInThisMonth(@Param("storeId") long storeId, @Param("userId") long userId,
//			@Param("productId") String productId);
	
	//New query:
	Map<String, Object> getTotalVolumeUserBoughtInThisMonth(@Param("storeId") long storeId, @Param("userId") long userId);

	Map<String, Object> getDaesangOrderById(@Param("orderId") String orderId, @Param("storeId") long storeId);

	int updateOrderDeliveryInfo(@Param("phone") String phone, @Param("deliveryAddress") String deliveryAddress,
			@Param("etmDeliveryTime") Date etmDeliveryTime, @Param("orderId") String orderId);

}
