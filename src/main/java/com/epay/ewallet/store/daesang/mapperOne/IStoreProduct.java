package com.epay.ewallet.store.daesang.mapperOne;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.epay.ewallet.store.daesang.model.Product;
import com.epay.ewallet.store.daesang.model.ProductDetail;

@Mapper
public interface IStoreProduct {
	
	Map<String, Object> getProductAndThreshold(@Param("storeId") long storeId, @Param("productId") long productId);
	
	Map<String, Object> getProductAndThresholdByProductCode(@Param("storeId") long storeId, @Param("productCode") String productCode);
	
	List<Product> getAllActiveProductsInPage(@Param("storeId") Long storeId, @Param("start") int start, @Param("end") int end);

    List<Product> getAllActiveProductsByNameInPage(@Param("storeId") Long storeId, @Param("productName") String productName, @Param("start") int start, @Param("end") int end);

    List<ProductDetail> getProductsById(@Param("productId") Long productId, @Param("storeId") Long storeId);
    
    List<Map<String, Object>> getOrderProductInfoByListCode(@Param("listProductCode") Map<String, Object> listProductCode, @Param("orderId") String orderId);
    
    Map<String, String> getProductCodeByID(@Param("productId") long productId);
}
