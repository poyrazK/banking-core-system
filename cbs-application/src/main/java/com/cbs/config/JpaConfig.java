package com.cbs.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.cbs")
@EnableJpaRepositories(basePackages = "com.cbs")
public class JpaConfig {
}
