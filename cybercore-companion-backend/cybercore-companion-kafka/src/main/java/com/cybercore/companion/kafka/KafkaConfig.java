package com.cybercore.companion.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic corelingInteractionsTopic() {
        return new NewTopic("coreling.interactions", 2, (short) 1);
    }
}
