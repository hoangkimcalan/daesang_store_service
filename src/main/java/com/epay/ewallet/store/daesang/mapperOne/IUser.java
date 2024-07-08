package com.epay.ewallet.store.daesang.mapperOne;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.epay.ewallet.store.daesang.model.User;

@Mapper
public interface IUser {

	User getUserByPhoneNumber(String phone);

	User getUserById(@Param("id") Long id);
	
	Map<String, String> getUserFullNameByPhone(@Param("phone") String phone);

}
