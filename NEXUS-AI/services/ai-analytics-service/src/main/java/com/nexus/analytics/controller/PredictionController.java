package com.nexus.analytics.controller;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

@RestController
public class PredictionController {

    private final MultiLayerNetwork model;

    public PredictionController(MultiLayerNetwork model) {
        this.model = model;
    }

    @PostMapping("/predict-load")
    public String predictLoad(@RequestBody double[] recentMetrics) {
        INDArray input = Nd4j.create(recentMetrics).reshape(1, 1, recentMetrics.length);

        INDArray output = model.output(input);
        double predictedLoad = output.getDouble(0);

        return "Przewidywane obciążenie: " + predictedLoad;
    }
}