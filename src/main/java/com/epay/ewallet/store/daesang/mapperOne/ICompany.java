package com.epay.ewallet.store.daesang.mapperOne;

import org.apache.ibatis.annotations.Mapper;

import com.epay.ewallet.store.daesang.model.Company;

@Mapper
public interface ICompany {
    Company getCompanyById(long companyId);
}
