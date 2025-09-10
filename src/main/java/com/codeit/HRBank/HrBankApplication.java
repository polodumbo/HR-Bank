package com.codeit.HRBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HrBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrBankApplication.class, args);
	}

}
