package com.epay.ewallet.store.daesang.entities;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyOnDayOfWeekConfiguration {
	
	private int day; // Day of week can purchase
	private int startHour; //Time purchase must be greater than or equal to startHour
	private int endHour; //Time purchase must be less than or equal to endHour
	
}
