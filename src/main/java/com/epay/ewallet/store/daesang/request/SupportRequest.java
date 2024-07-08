package com.epay.ewallet.store.daesang.request;

import lombok.Data;

import java.util.List;

@Data
public class SupportRequest {
    private String categoryId;
    private String content;
    private String transactionId;
    private List<String> images;

}


