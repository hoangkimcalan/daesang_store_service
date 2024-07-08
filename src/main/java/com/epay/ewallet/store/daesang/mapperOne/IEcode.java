package com.epay.ewallet.store.daesang.mapperOne;

import java.util.HashMap;

import org.apache.ibatis.annotations.Mapper;

import com.epay.ewallet.store.daesang.model.Ecode;

@Mapper
public interface IEcode {

	HashMap<String, String> getMessageByCode(HashMap<String, String> map);

	Ecode getEcode(HashMap<String, String> map);
}
