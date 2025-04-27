package br.com.yonlero.apportionment.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ApportionmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApportionmentServiceApplication.class, args);
	}

}
