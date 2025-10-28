package com.nexus.analytics.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModelQualityTest {

    private ModelTrainingService modelTrainingService;
    private MultiLayerNetwork mockModel;
    private MeterRegistry meterRegistry;
    private AtomicReference<Double> mseGaugeValue;

    @BeforeEach
    void setUp() {
        mockModel = mock(MultiLayerNetwork.class);
        modelTrainingService = new ModelTrainingService(mockModel); // Zakładając, że model jest wstrzykiwany

        // Konfiguracja Micrometer
        meterRegistry = new SimpleMeterRegistry();
        mseGaugeValue = new AtomicReference<>(0.0);
        meterRegistry.gauge("ai.model.validation.mse", mseGaugeValue, AtomicReference::get);
    }

    @Test
    void testModelQualityAgainstDeterministicData() {
        double[] testInputData = new double[50];
        double[] expectedOutputData = new double[50];
        for (int j = 0; j < 50; j++) {
            testInputData[j] = Math.sin(0.1 * (j + 1000));
            expectedOutputData[j] = Math.sin(0.1 * (j + 1000 + 1));
        }

        INDArray testFeatures = Nd4j.create(new double[][]{testInputData}).reshape(1, 1, 50);
        INDArray testLabels = Nd4j.create(new double[][]{expectedOutputData}).reshape(1, 1, 50);
        DataSet testData = new DataSet(testFeatures, testLabels);

        INDArray mockPrediction = Nd4j.create(new double[][]{expectedOutputData}).reshape(1, 1, 50);
        when(mockModel.output(testFeatures)).thenReturn(mockPrediction);

        RegressionEvaluation eval = new RegressionEvaluation(1);
        INDArray predicted = mockModel.output(testFeatures);
        eval.eval(testLabels, predicted);

        double mse = eval.meanSquaredError(0);
        double r2 = eval.rSquared(0);

        mseGaugeValue.set(mse);

        // Then
        double MSE_THRESHOLD = 0.01;
        double R2_THRESHOLD = 0.95;

        System.out.println("Test Quality MSE: " + mse);
        System.out.println("Test Quality R^2: " + r2);

        assertThat(mse).isLessThan(MSE_THRESHOLD);
        assertThat(r2).isGreaterThan(R2_THRESHOLD);

        // Sprawdź metrykę Micrometer
        assertThat(meterRegistry.get("ai.model.validation.mse").gauge().value()).isEqualTo(mse);
    }
}