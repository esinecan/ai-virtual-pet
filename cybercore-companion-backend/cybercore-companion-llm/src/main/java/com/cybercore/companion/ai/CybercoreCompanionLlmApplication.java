package com.cybercore.companion.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class CybercoreCompanionLlmApplication {
    public static void main(String[] args) {
        SpringApplication.run(CybercoreCompanionLlmApplication.class, args);
    }
}
