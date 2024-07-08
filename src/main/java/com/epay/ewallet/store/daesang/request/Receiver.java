package com.epay.ewallet.store.daesang.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Receiver {
	
	private String name;
	private String address;
	private String phone;
	private String email;
	
}
