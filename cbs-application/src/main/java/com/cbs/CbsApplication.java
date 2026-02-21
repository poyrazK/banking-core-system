package com.cbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbsApplication.class, args);
    }
}
