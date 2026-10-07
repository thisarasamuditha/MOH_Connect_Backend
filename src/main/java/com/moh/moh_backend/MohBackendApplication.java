package com.moh.moh_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MohBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MohBackendApplication.class, args);
	}

}
