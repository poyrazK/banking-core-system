package com.cbs.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class CbsConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbsConfigServerApplication.class, args);
    }
}
