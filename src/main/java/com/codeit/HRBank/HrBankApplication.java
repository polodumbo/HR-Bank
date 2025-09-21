package com.codeit.HRBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableJpaAuditing // JPA Auditing 활성화 (생성일시 자동 주입)
@EnableScheduling // 스프링 스케줄러 활성화
public class HrBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrBankApplication.class, args);
    }

}
