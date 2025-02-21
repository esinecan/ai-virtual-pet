package com.cybercore.companion.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
@SpringBootTest(classes = KafkaConfig.class)
class KafkaConfigTest {

    @Test
    void contextLoads() {
    }
}