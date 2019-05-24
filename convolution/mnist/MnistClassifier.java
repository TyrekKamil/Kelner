package mnist; /*******************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/


import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.schedule.MapSchedule;
import org.nd4j.linalg.schedule.ScheduleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
/*******************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;

/**
 * Common data utility functions.
 *
 * @author fvaleri
 */
class DataUtilities {

    /**
     * Download a remote file if it doesn't exist.
     * @param remoteUrl URL of the remote file.
     * @param localPath Where to download the file.
     * @return True if and only if the file has been downloaded.
     * @throws Exception IO error.
     */
    public static boolean downloadFile(String remoteUrl, String localPath) throws IOException {
        boolean downloaded = false;
        if (remoteUrl == null || localPath == null)
            return downloaded;
        File file = new File(localPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            HttpClientBuilder builder = HttpClientBuilder.create();
            CloseableHttpClient client = builder.build();
            try (CloseableHttpResponse response = client.execute(new HttpGet(remoteUrl))) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try (FileOutputStream outstream = new FileOutputStream(file)) {
                        entity.writeTo(outstream);
                        outstream.flush();
                        outstream.close();
                    }
                }
            }
            downloaded = true;
        }
        if (!file.exists())
            throw new IOException("File doesn't exist: " + localPath);
        return downloaded;
    }

    /**
     * Extract a "tar.gz" file into a local folder.
     * @param inputPath Input file path.
     * @param outputPath Output directory path.
     * @throws IOException IO error.
     */
    public static void extractTarGz(String inputPath, String outputPath) throws IOException {
        if (inputPath == null || outputPath == null)
            return;
        final int bufferSize = 4096;
        if (!outputPath.endsWith("" + File.separatorChar))
            outputPath = outputPath + File.separatorChar;
        try (TarArchiveInputStream tais = new TarArchiveInputStream(
                new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(inputPath))))) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tais.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    new File(outputPath + entry.getName()).mkdirs();
                } else {
                    int count;
                    byte data[] = new byte[bufferSize];
                    FileOutputStream fos = new FileOutputStream(outputPath + entry.getName());
                    BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
                    while ((count = tais.read(data, 0, bufferSize)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.close();
                }
            }
        }
    }

}
/**
 * Implementation of LeNet-5 for handwritten digits image classification on MNIST dataset (99% accuracy)
 * <a href="http://yann.lecun.com/exdb/publis/pdf/lecun-01a.pdf">[LeCun et al., 1998. Gradient based learning applied to document recognition]</a>
 * Some minor changes are made to the architecture like using ReLU and identity activation instead of
 * sigmoid/tanh, max pooling instead of avg pooling and softmax output layer.
 * <p>
 * This example will download 15 Mb of data on the first run.
 *
 * @author hanlon
 * @author agibsonccc
 * @author fvaleri
 * @author dariuszzbyrad
 */
public class MnistClassifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(MnistClassifier.class);
    private static final String BASE_PATH = System.getProperty("java.io.tmpdir") + "/mnist";
    private static final String DATA_URL = "http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz";

    public static void main(String[] args) throws Exception {
        int height = 28;    // height of the picture in px
        int width = 28;     // width of the picture in px
        int channels = 1;   // single channel for grayscale images
        int outputNum = 10; // 10 digits classification
        int batchSize = 54; // number of samples that will be propagated through the network in each iteration
        int nEpochs = 1;    // number of training epochs

        int seed = 1234;    // number used to initialize a pseudorandom number generator.
        Random randNumGen = new Random(seed);

        LOGGER.info("Data load...");
        if (!new File(BASE_PATH + "/mnist_png").exists()) {

            LOGGER.debug("Data downloaded from {}", DATA_URL);
            String localFilePath = BASE_PATH + "/mnist_png.tar.gz";
            if (DataUtilities.downloadFile(DATA_URL, localFilePath)) {
                DataUtilities.extractTarGz(localFilePath, BASE_PATH);
            }
        }

        LOGGER.info("Data vectorization...");
        // vectorization of train data
        File trainData = new File(BASE_PATH + "/mnist_png/training");
        FileSplit trainSplit = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator(); // use parent directory name as the image label
        ImageRecordReader trainRR = new ImageRecordReader(height, width, channels, labelMaker);
        trainRR.initialize(trainSplit);
        DataSetIterator trainIter = new RecordReaderDataSetIterator(trainRR, batchSize, 1, outputNum);

        // pixel values from 0-255 to 0-1 (min-max scaling)
        DataNormalization imageScaler = new ImagePreProcessingScaler();
        imageScaler.fit(trainIter);
        trainIter.setPreProcessor(imageScaler);

        // vectorization of test data
        File testData = new File(BASE_PATH + "/mnist_png/testing");
        FileSplit testSplit = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
        ImageRecordReader testRR = new ImageRecordReader(height, width, channels, labelMaker);
        testRR.initialize(testSplit);
        DataSetIterator testIter = new RecordReaderDataSetIterator(testRR, batchSize, 1, outputNum);
        testIter.setPreProcessor(imageScaler); // same normalization for better results

        LOGGER.info("Network configuration and training...");
        // reduce the learning rate as the number of training epochs increases
        // iteration #, learning rate
        Map<Integer, Double> learningRateSchedule = new HashMap<>();
        learningRateSchedule.put(0, 0.06);
        learningRateSchedule.put(200, 0.05);
        learningRateSchedule.put(600, 0.028);
        learningRateSchedule.put(800, 0.0060);
        learningRateSchedule.put(1000, 0.001);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .l2(0.0005) // ridge regression value
            .updater(new Nesterovs(new MapSchedule(ScheduleType.ITERATION, learningRateSchedule)))
            .weightInit(WeightInit.XAVIER)
            .list()
            .layer(new ConvolutionLayer.Builder(5, 5)
                .nIn(channels)
                .stride(1, 1)
                .nOut(20)
                .activation(Activation.IDENTITY)
                .build())
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            .layer(new ConvolutionLayer.Builder(5, 5)
                .stride(1, 1) // nIn need not specified in later layers
                .nOut(50)
                .activation(Activation.IDENTITY)
                .build())
            .layer(new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                .kernelSize(2, 2)
                .stride(2, 2)
                .build())
            .layer(new DenseLayer.Builder().activation(Activation.RELU)
                .nOut(500)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(outputNum)
                .activation(Activation.SOFTMAX)
                .build())
            .setInputType(InputType.convolutionalFlat(height, width, channels)) // InputType.convolutional for normal image
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(10));
        LOGGER.info("Total num of params: {}", net.numParams());

        // evaluation while training (the score should go down)
        for (int i = 0; i < nEpochs; i++) {
            net.fit(trainIter);
            LOGGER.info("Completed epoch {}", i);
            Evaluation eval = net.evaluate(testIter);
            LOGGER.info(eval.stats());

            trainIter.reset();
            testIter.reset();
        }

        File ministModelPath = new File(BASE_PATH + "/minist-model.zip");
        ModelSerializer.writeModel(net, ministModelPath, true);
        LOGGER.info("The MINIST model has been saved in {}", ministModelPath.getPath());
    }
}
