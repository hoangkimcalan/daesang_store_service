package com.epay.ewallet.store.daesang.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TBL_STORE_BANNER
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Banner {
    private long id;
    private String image;
    private int priority;
}
