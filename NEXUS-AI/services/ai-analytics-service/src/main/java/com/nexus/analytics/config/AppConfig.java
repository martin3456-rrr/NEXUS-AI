package com.nexus.analytics.config;

import com.nexus.analytics.service.ModelTrainingService;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ExecutionException;

@Configuration
public class AppConfig {
    @Bean
    public MultiLayerNetwork multiLayerNetwork(ModelTrainingService trainingService)
            throws ExecutionException, InterruptedException {
        System.out.println("Initiating model training...");
        MultiLayerNetwork model = trainingService.createAndTrainModel().get();
        System.out.println("Model is ready for predictions.");
        return model;
    }
}