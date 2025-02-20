package com.sparta.taptoon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class TaptoonApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaptoonApplication.class, args);
	}

}
