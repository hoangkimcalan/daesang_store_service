package com.epay.ewallet.store.daesang.mapperOne;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.epay.ewallet.store.daesang.model.Banner;
import com.epay.ewallet.store.daesang.model.StoreFixedDeliveryAddress;
import com.epay.ewallet.store.daesang.model.Product;
import com.epay.ewallet.store.daesang.model.ProductDetail;
import com.epay.ewallet.store.daesang.model.Store;

@Mapper
public interface IStore {
    Store getStoreById(long storeId);
    List<Banner> getBannersByStoreIdSortByPriorityAsc(long storeId);

    List<StoreFixedDeliveryAddress> getDeliveryAddressByStoreId(long storeId);
    
    List<Map<String, String>> getStorePaymentMethod(@Param("merchantGroupId") String merchantGroupId, @Param("serviceId") String serviceId);
    
    StoreFixedDeliveryAddress getDeliveryAddress(@Param("id") long id, @Param("storeId") long storeId);
    
}
