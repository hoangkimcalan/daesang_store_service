package com.epay.ewallet.store.daesang.model;

import lombok.Data;

@Data
public class Company {

    private Long id;

    private String shortName;

    private String logo;

    private String logoSocial;

    private String phoneNumber;

    private String website;

    private int status;
}
