package com.nexus.analytics.service;

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ModelTrainingService {

    public MultiLayerNetwork createAndTrainModel() {
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

        int numSamples = 100;
        int timeSeriesLength = 50;
        int batchSize = 10;
        int numberOfEpochs = 5;

        DataSet allData = createDummySequenceData(numSamples, timeSeriesLength);
        List<DataSet> list = allData.asList();
        Collections.shuffle(list);
        ListDataSetIterator<DataSet> trainData = new ListDataSetIterator<>(list, batchSize);

        // Trening modelu
        for (int i = 0; i < numberOfEpochs; i++) {
            trainData.reset();
            model.fit(trainData);
            System.out.println("Epoka " + (i + 1) + " zakończona");
        }
        System.out.println("Model został pomyślnie wytrenowany.");
        return model;
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
        DataSet allData = DataSet.merge(listDs);
        return allData;
    }

}