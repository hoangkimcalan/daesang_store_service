package com.epay.ewallet.store.daesang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.epay.ewallet" })
public class StoreDaesangApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoreDaesangApplication.class, args);
	}
	
}
