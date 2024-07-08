package com.epay.ewallet.store.daesang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TBL_STORE
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Store {
    private long id;
    private String name;
    private int status;
}