package com.nexus.analytics.controller;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import com.nexus.analytics.service.ModelTrainingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "AI Prediction", description = "Endpoints for AI model predictions")
public class PredictionController {

    private final ModelTrainingService modelTrainingService;

    public PredictionController(ModelTrainingService modelTrainingService) {
        this.modelTrainingService = modelTrainingService;
    }

    @PostMapping("/predict-load")
    public String predictLoad(@RequestBody double[] recentMetrics) {
        INDArray input = Nd4j.create(recentMetrics).reshape(1, 1, recentMetrics.length);

        MultiLayerNetwork currentModel = modelTrainingService.getModel();

        INDArray output = currentModel.output(input);
        double predictedLoad = output.getDouble(0);

        return "Przewidywane obciążenie: " + predictedLoad;
    }
}