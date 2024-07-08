package com.epay.ewallet.store.daesang.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChooseCartProductRequest {
	
	private List<String> chosenList;
	
}
