package com.nexus.analytics.service;

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.evaluation.regression.RegressionEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ModelTrainingService {
    private MultiLayerNetwork currentModel;

    @Autowired
    public ModelTrainingService(MultiLayerNetwork initialModel) {
        this.currentModel = initialModel;
    }

    public MultiLayerNetwork getModel() {
        return this.currentModel;
    }

    /**
     * Perform a prediction using the current model.
     * @param input INDArray shaped as required by the model
     * @return INDArray with the model output
     */
    public INDArray predict(INDArray input) {
        return this.currentModel.output(input);
    }

    @Async
    public CompletableFuture<MultiLayerNetwork> createAndTrainModel() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.005))
                .list()
                .layer(0, new LSTM.Builder().nIn(1).nOut(50)
                        .activation(Activation.TANH).build())
                .layer(1, new DenseLayer.Builder().nIn(50).nOut(1)
                        .activation(Activation.IDENTITY).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();

        int batchSize = 10;

        DataSet allData = createDummySequenceData(200, 50);

        allData.shuffle();
        org.nd4j.linalg.dataset.SplitTestAndTrain split = allData.splitTestAndTrain(0.8);
        DataSet trainingData = split.getTrain();
        DataSet testData = split.getTest();

        List<DataSet> trainList = trainingData.asList();
        ListDataSetIterator<DataSet> trainDataIterator = new ListDataSetIterator<>(trainList, batchSize);

        int numSamples = 100;
        int timeSeriesLength = 50;

        int numberOfEpochs = 5;

        //Trening
        for (int i = 0; i < numberOfEpochs; i++) {
            trainDataIterator.reset();
            model.fit(trainDataIterator);
        }
        System.out.println("Rozpoczynanie ewaluacji modelu...");
        RegressionEvaluation eval = new RegressionEvaluation(1);
        INDArray features = testData.getFeatures();
        INDArray labels = testData.getLabels();
        INDArray predicted = model.output(features);

        eval.eval(labels, predicted);

        System.out.println("Mean Squared Error (MSE): " + eval.meanSquaredError(0));
        System.out.println("R^2 Score: " + eval.rSquared(0));
        System.out.println("Ewaluacja zakończona.");


        System.out.println("Model został pomyślnie wytrenowany.");
        return CompletableFuture.completedFuture(model);
    }
    public DataSet createDummySequenceData(int numSamples, int timeSeriesLength) {
        List<DataSet> listDs = new ArrayList<>();
        for (int i = 0; i < numSamples; i++) {
            double[] inputArray = new double[timeSeriesLength];
            double[] outputArray = new double[timeSeriesLength];
            for (int j = 0; j < timeSeriesLength; j++) {
                inputArray[j] = Math.sin(0.1 * (j + i));
                outputArray[j] = Math.sin(0.1 * (j + i + 1));
            }
            INDArray features = Nd4j.create(new double[][]{inputArray}).reshape(1, 1, timeSeriesLength);
            INDArray labels = Nd4j.create(new double[][]{outputArray}).reshape(1, 1, timeSeriesLength);
            listDs.add(new DataSet(features, labels));
        }
        return DataSet.merge(listDs);
    }
    @Async
    @Scheduled(fixedRate = 86400000) // Uruchamiaj co 24 godziny (w milisekundach)
    public void retrainModelScheduled() throws ExecutionException, InterruptedException {
        System.out.println("Rozpoczynanie cyklicznego treningu modelu...");
        MultiLayerNetwork newModel = createAndTrainModel().get();
        this.currentModel = newModel;
        System.out.println("Cykliczny trening modelu zakończony. Model został zaktualizowany.");
    }

}