package com.nexus.analytics.controller;

import com.nexus.analytics.service.ModelTrainingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PredictionController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.cloud.config.enabled=false"
})
class PredictionControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ModelTrainingService modelTrainingService;

    @Test
    void predict_happy_path() throws Exception {
        INDArray mockOutput = Nd4j.createFromArray(new double[]{0.75}).reshape(1,1);
        Mockito.when(modelTrainingService.predict(Mockito.any(INDArray.class))).thenReturn(mockOutput);

        mvc.perform(post("/api/analytics/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"input\":[1.0,2.0,3.0]" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").value(0.75))
                .andExpect(jsonPath("$.mse").exists());
    }

    @Test
    void predict_validation_error() throws Exception {
        mvc.perform(post("/api/analytics/api/ai/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void predict_internal_error() throws Exception {
        Mockito.when(modelTrainingService.predict(Mockito.any(INDArray.class)))
                .thenThrow(new RuntimeException("Model failure"));

        mvc.perform(post("/api/analytics/api/ai/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"input\":[1.0]" +
                                "}"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void predict_shouldReturn500OnModelFailure() throws Exception {
        // Given
        String errorMessage = "Błąd ładowania modelu";
        Mockito.when(modelTrainingService.predict(Mockito.any(INDArray.class)))
                .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mvc.perform(post("/api/analytics/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\":[1.0, 2.0]}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}
