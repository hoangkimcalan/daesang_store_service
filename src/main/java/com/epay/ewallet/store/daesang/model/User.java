package com.epay.ewallet.store.daesang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private long id;
    private String phoneNumber;
    private String name;
    private String email;
    private int status;
    private String personalId;
    private String personalIdType;
    private String address;
    private String lang;
    private String password;
    private String companyId;
    private int empVerifyStatus;
}
