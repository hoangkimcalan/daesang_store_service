package com.epay.ewallet.store.daesang.mapperOne;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.epay.ewallet.store.daesang.model.DucVietCart;

@Mapper
public interface IDucVietCart {

	DucVietCart getCartProductByID(@Param("cartProductId") String cartProductId);

	int countTotalProductInUserCart(@Param("userId") long userId);

	void addNewProductToCart(@Param("cartProduct") DucVietCart cartProduct);

	void changeQuantity(@Param("cartProduct") DucVietCart cartProduct);

	List<Map<String, Object>> getUserCartDetail(@Param("userId") long userId, @Param("start") int start,
			@Param("end") int end);

	void removeProductFromCart(@Param("id") String cartProductId);

	Map<String, BigDecimal> sumCartProductAmount(@Param("userId") long userId,
			@Param("listProductCode") List<String> listProductCode);
	
	List<Map<String, Object>> loadListCartProductCheckout(@Param("userId") long userId,
			@Param("listProductCode") List<String> listProductCode);
	
	List<Map<String, Object>> loadListCartProductCheckoutSuccess(@Param("userId") long userId,
			@Param("listProductCode") List<String> listProductCode);
	
	void removeProductsFromCart(@Param("listCartProductId") List<String> listCartProductId);
	
	Map<String, BigDecimal> countTotalCartItemByUserId(@Param("userId") long userId);
}
