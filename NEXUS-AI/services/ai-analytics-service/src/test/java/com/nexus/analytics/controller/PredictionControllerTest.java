package com.nexus.analytics.controller;

import com.nexus.analytics.service.ModelTrainingService;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.jupiter.api.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.nd4j.linalg.indexing.conditions.Conditions.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PredictionController.class)
@ActiveProfiles("test")
public class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModelTrainingService modelTrainingService;

    @MockitoBean
    private MultiLayerNetwork multiLayerNetwork;

    @Test
    void shouldPredictCorrectlyWithRealData() throws Exception {
        double[] inputMetrics = {0.1, 0.2, 0.3, 0.4, 0.5};  // 5 próbek historycznych
        double expectedPrediction = 0.6;  // Oczekiwana predykcja (z trenowania)
        INDArray inputArray = Nd4j.create(inputMetrics);
        INDArray mockOutput = Nd4j.create(new double[]{expectedPrediction});

        when(modelTrainingService.getModel()).thenReturn(multiLayerNetwork);
        when(multiLayerNetwork.output(any(INDArray.class))).thenReturn(mockOutput);  // Lub when(modelTrainingService.predict(inputArray)).thenReturn(mockOutput);

        String jsonInput = "{\"input\": [0.1, 0.2, 0.3, 0.4, 0.5]}";  // Lub prosty array: "[0.1, 0.2, 0.3, 0.4, 0.5]"

        mockMvc.perform(post("/predict-load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").value(expectedPrediction))
                .andExpect(jsonPath("$.mse").value(lessThan(0.05)))
                .andExpect(content().string(containsString("Przewidywane obciążenie: " + expectedPrediction)));
    }

    @Test
    void shouldHandleInvalidInput() throws Exception {
        String invalidJsonInput = "{\"input\": []}";

        mockMvc.perform(post("/predict-load")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonInput))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input data")));
    }
}
