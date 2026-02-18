package com.cbs.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
class ConfigServerIntegrationTest {

    @Test
    void contextLoads() {
    }
}
