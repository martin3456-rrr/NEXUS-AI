package com.nexus.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {
        // Retry 3 razy z przerwami 2 sekund, potem DLT lub logowanie błędu
        return new DefaultErrorHandler((consumerRecord, exception) -> {
            System.err.println("Nieudane przetworzenie eventu po 3 próbach: " + consumerRecord.value());
            // Tu możesz dodać logowanie lub wysłanie do Dead Letter Topic (DLT)
        }, new FixedBackOff(2000L, 2L)); // 2000ms przerwy, max 2 ponowienia (razem 3 próby)
    }
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
