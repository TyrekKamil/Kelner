package sentenceclassification; /*******************************************************************************
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator;
import org.deeplearning4j.iterator.LabeledSentenceProvider;
import org.deeplearning4j.iterator.provider.FileLabeledSentenceProvider;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * This is a DataSetIterator that is specialized for the IMDB review dataset used in the Word2VecSentimentRNN example
 * It takes either the train or test set data from this data set, plus a WordVectors object (typically the Google News
 * 300 pretrained vectors from https://code.google.com/p/word2vec/) and generates training data sets.<br>
 * Inputs/features: variable-length time series, where each word (with unknown words removed) is represented by
 * its Word2Vec vector representation.<br>
 * Labels/target: a single class (negative or positive), predicted at the final time step (word) of each review
 *
 * @author Alex Black
 */
class SentimentExampleIterator implements DataSetIterator
{
    private final WordVectors wordVectors;
    private final int batchSize;
    private final int vectorSize;
    private final int truncateLength;

    private int cursor = 0;
    private final File[] positiveFiles;
    private final File[] negativeFiles;
    private final TokenizerFactory tokenizerFactory;

    /**
     * @param dataDirectory  the directory of the IMDB review data set
     * @param wordVectors    WordVectors object
     * @param batchSize      Size of each minibatch for training
     * @param truncateLength If reviews exceed
     * @param train          If true: return the training data. If false: return the testing data.
     */
    public SentimentExampleIterator(String dataDirectory, WordVectors wordVectors, int batchSize, int truncateLength, boolean train) throws IOException
    {
        this.batchSize = batchSize;
        this.vectorSize = wordVectors.getWordVector(wordVectors.vocab().wordAtIndex(0)).length;


        File p = new File(FilenameUtils.concat(dataDirectory, "aclImdb/" + (train ? "train" : "test") + "/pos/") + "/");
        File n = new File(FilenameUtils.concat(dataDirectory, "aclImdb/" + (train ? "train" : "test") + "/neg/") + "/");
        positiveFiles = p.listFiles();
        negativeFiles = n.listFiles();

        this.wordVectors = wordVectors;
        this.truncateLength = truncateLength;

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
    }


    @Override
    public DataSet next(int num)
    {
        if (cursor >= positiveFiles.length + negativeFiles.length) throw new NoSuchElementException();
        try
        {
            return nextDataSet(num);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private DataSet nextDataSet(int num) throws IOException
    {
        //First: load reviews to String. Alternate positive and negative reviews
        List<String> reviews = new ArrayList<>(num);
        boolean[] positive = new boolean[num];
        for (int i = 0; i < num && cursor < totalExamples(); i++)
        {
            if (cursor % 2 == 0)
            {
                //Load positive review
                int posReviewNumber = cursor / 2;
                String review = FileUtils.readFileToString(positiveFiles[posReviewNumber]);
                reviews.add(review);
                positive[i] = true;
            } else
            {
                //Load negative review
                int negReviewNumber = cursor / 2;
                String review = FileUtils.readFileToString(negativeFiles[negReviewNumber]);
                reviews.add(review);
                positive[i] = false;
            }
            cursor++;
        }

        //Second: tokenize reviews and filter out unknown words
        List<List<String>> allTokens = new ArrayList<>(reviews.size());
        int maxLength = 0;
        for (String s : reviews)
        {
            List<String> tokens = tokenizerFactory.create(s).getTokens();
            List<String> tokensFiltered = new ArrayList<>();
            for (String t : tokens)
            {
                if (wordVectors.hasWord(t)) tokensFiltered.add(t);
            }
            allTokens.add(tokensFiltered);
            maxLength = Math.max(maxLength, tokensFiltered.size());
        }

        //If longest review exceeds 'truncateLength': only take the first 'truncateLength' words
        if (maxLength > truncateLength) maxLength = truncateLength;

        //Create data for training
        //Here: we have reviews.size() examples of varying lengths
        INDArray features = Nd4j.create(new int[]{reviews.size(), vectorSize, maxLength}, 'f');
        INDArray labels = Nd4j.create(new int[]{reviews.size(), 2, maxLength}, 'f');    //Two labels: positive or negative
        //Because we are dealing with reviews of different lengths and only one output at the final time step: use padding arrays
        //Mask arrays contain 1 if data is present at that time step for that example, or 0 if data is just padding
        INDArray featuresMask = Nd4j.zeros(reviews.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(reviews.size(), maxLength);

        for (int i = 0; i < reviews.size(); i++)
        {
            List<String> tokens = allTokens.get(i);

            // Get the truncated sequence length of document (i)
            int seqLength = Math.min(tokens.size(), maxLength);

            // Get all wordvectors for the current document and transpose them to fit the 2nd and 3rd feature shape
            final INDArray vectors = wordVectors.getWordVectors(tokens.subList(0, seqLength)).transpose();

            // Put wordvectors into features array at the following indices:
            // 1) Document (i)
            // 2) All vector elements which is equal to NDArrayIndex.interval(0, vectorSize)
            // 3) All elements between 0 and the length of the current sequence
            features.put(
                    new INDArrayIndex[]{
                            NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.interval(0, seqLength)
                    },
                    vectors);

            // Assign "1" to each position where a feature is present, that is, in the interval of [0, seqLength)
            featuresMask.get(new INDArrayIndex[]{NDArrayIndex.point(i), NDArrayIndex.interval(0, seqLength)}).assign(1);

            int idx = (positive[i] ? 0 : 1);
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[]{i, idx, lastIdx - 1}, 1.0);   //Set label: [0,1] for negative, [1,0] for positive
            labelsMask.putScalar(new int[]{i, lastIdx - 1}, 1.0);   //Specify that an output exists at the final time step for this example
        }

        return new DataSet(features, labels, featuresMask, labelsMask);
    }

    public int totalExamples()
    {
        return positiveFiles.length + negativeFiles.length;
    }

    @Override
    public int inputColumns()
    {
        return vectorSize;
    }

    @Override
    public int totalOutcomes()
    {
        return 2;
    }

    @Override
    public void reset()
    {
        cursor = 0;
    }

    public boolean resetSupported()
    {
        return true;
    }

    @Override
    public boolean asyncSupported()
    {
        return true;
    }

    @Override
    public int batch()
    {
        return batchSize;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getLabels()
    {
        return Arrays.asList("positive", "negative");
    }

    @Override
    public boolean hasNext()
    {
        return cursor < totalExamples();
    }

    @Override
    public DataSet next()
    {
        return next(batchSize);
    }

    @Override
    public void remove()
    {

    }

    @Override
    public DataSetPreProcessor getPreProcessor()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Convenience method for loading review to String
     */
    public String loadReviewToString(int index) throws IOException
    {
        File f;
        if (index % 2 == 0) f = positiveFiles[index / 2];
        else f = negativeFiles[index / 2];
        return FileUtils.readFileToString(f);
    }

    /**
     * Convenience method to get label for review
     */
    public boolean isPositiveReview(int index)
    {
        return index % 2 == 0;
    }

    /**
     * Used post training to load a review from a file to a features INDArray that can be passed to the network output method
     *
     * @param file      File to load the review from
     * @param maxLength Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array
     * @throws IOException If file cannot be read
     */
    public INDArray loadFeaturesFromFile(File file, int maxLength) throws IOException
    {
        String review = FileUtils.readFileToString(file);
        return loadFeaturesFromString(review, maxLength);
    }

    /**
     * Used post training to convert a String to a features INDArray that can be passed to the network output method
     *
     * @param reviewContents Contents of the review to vectorize
     * @param maxLength      Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array for the given input String
     */
    public INDArray loadFeaturesFromString(String reviewContents, int maxLength)
    {
        List<String> tokens = tokenizerFactory.create(reviewContents).getTokens();
        List<String> tokensFiltered = new ArrayList<>();
        for (String t : tokens)
        {
            if (wordVectors.hasWord(t)) tokensFiltered.add(t);
        }
        int outputLength = Math.min(maxLength, tokensFiltered.size());

        INDArray features = Nd4j.create(1, vectorSize, outputLength);

        int count = 0;
        for (int j = 0; j < tokensFiltered.size() && count < maxLength; j++)
        {
            String token = tokensFiltered.get(j);
            INDArray vector = wordVectors.getWordVectorMatrix(token);
            if (vector == null)
            {
                continue;   //Word not in word vectors
            }
            features.put(new INDArrayIndex[]{NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j)}, vector);
            count++;
        }

        return features;
    }
}

/**
 * Common data utility functions.
 *
 * @author fvaleri
 */
class DataUtilities
{

    /**
     * Download a remote file if it doesn't exist.
     *
     * @param remoteUrl URL of the remote file.
     * @param localPath Where to download the file.
     * @return True if and only if the file has been downloaded.
     * @throws Exception IO error.
     */
    public static boolean downloadFile(String remoteUrl, String localPath) throws IOException
    {
        boolean downloaded = false;
        if (remoteUrl == null || localPath == null)
            return downloaded;
        File file = new File(localPath);
        if (!file.exists())
        {
            file.getParentFile().mkdirs();
            HttpClientBuilder builder = HttpClientBuilder.create();
            CloseableHttpClient client = builder.build();
            try (CloseableHttpResponse response = client.execute(new HttpGet(remoteUrl)))
            {
                HttpEntity entity = response.getEntity();
                if (entity != null)
                {
                    try (FileOutputStream outstream = new FileOutputStream(file))
                    {
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
     *
     * @param inputPath  Input file path.
     * @param outputPath Output directory path.
     * @throws IOException IO error.
     */
    public static void extractTarGz(String inputPath, String outputPath) throws IOException
    {
        if (inputPath == null || outputPath == null)
            return;
        final int bufferSize = 4096;
        if (!outputPath.endsWith("" + File.separatorChar))
            outputPath = outputPath + File.separatorChar;
        try (TarArchiveInputStream tais = new TarArchiveInputStream(
                new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(inputPath)))))
        {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tais.getNextEntry()) != null)
            {
                if (entry.isDirectory())
                {
                    new File(outputPath + entry.getName()).mkdirs();
                } else
                {
                    int count;
                    byte data[] = new byte[bufferSize];
                    FileOutputStream fos = new FileOutputStream(outputPath + entry.getName());
                    BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
                    while ((count = tais.read(data, 0, bufferSize)) != -1)
                    {
                        dest.write(data, 0, count);
                    }
                    dest.close();
                }
            }
        }
    }
}


class Word2VecSentimentRNN
{

    /**
     * Data URL for downloading
     */
    public static final String DATA_URL = "http://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz";
    /**
     * Location to save and extract the training/testing data
     */
    public static final String DATA_PATH = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "dl4j_w2vSentiment/");
    /**
     * Location (local file system) for the Google News vectors. Set this manually.
     */
    public static final String WORD_VECTORS_PATH = "/PATH/TO/YOUR/VECTORS/GoogleNews-vectors-negative300.bin.gz";


    public static void main(String[] args) throws Exception
    {
        if (WORD_VECTORS_PATH.startsWith("/PATH/TO/YOUR/VECTORS/"))
        {
            throw new RuntimeException("Please set the WORD_VECTORS_PATH before running this example");
        }

        //Download and extract data
        downloadData();

        int batchSize = 64;     //Number of examples in each minibatch
        int vectorSize = 300;   //Size of the word vectors. 300 in the Google News model
        int nEpochs = 1;        //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this
        final int seed = 0;     //Seed for reproducibility

        Nd4j.getMemoryManager().setAutoGcWindow(10000);  //https://deeplearning4j.org/workspaces

        //Set up network configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(5e-3))
                .l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue).gradientNormalizationThreshold(1.0)
                .list()
                .layer(new LSTM.Builder().nIn(vectorSize).nOut(256)
                        .activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        //DataSetIterators for training and testing respectively
        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(WORD_VECTORS_PATH));
        SentimentExampleIterator train = new SentimentExampleIterator(DATA_PATH, wordVectors, batchSize, truncateReviewsToLength, true);
        SentimentExampleIterator test = new SentimentExampleIterator(DATA_PATH, wordVectors, batchSize, truncateReviewsToLength, false);

        System.out.println("Starting training");
        net.setListeners(new ScoreIterationListener(1), new EvaluativeListener(test, 1, InvocationType.EPOCH_END));
        net.fit(train, nEpochs);

        //After training: load a single example and generate predictions
        File shortNegativeReviewFile = new File(FilenameUtils.concat(DATA_PATH, "aclImdb/test/neg/12100_1.txt"));
        String shortNegativeReview = FileUtils.readFileToString(shortNegativeReviewFile);

        INDArray features = test.loadFeaturesFromString(shortNegativeReview, truncateReviewsToLength);
        INDArray networkOutput = net.output(features);
        long timeSeriesLength = networkOutput.size(2);
        INDArray probabilitiesAtLastWord = networkOutput.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength - 1));

        System.out.println("\n\n-------------------------------");
        System.out.println("Short negative review: \n" + shortNegativeReview);
        System.out.println("\n\nProbabilities at last time step:");
        System.out.println("p(positive): " + probabilitiesAtLastWord.getDouble(0));
        System.out.println("p(negative): " + probabilitiesAtLastWord.getDouble(1));

        System.out.println("----- Example complete -----");
    }

    public static void downloadData() throws Exception
    {
        //Create directory if required
        File directory = new File(DATA_PATH);
        if (!directory.exists()) directory.mkdir();

        //Download file:
        String archizePath = DATA_PATH + "aclImdb_v1.tar.gz";
        File archiveFile = new File(archizePath);
        String extractedPath = DATA_PATH + "aclImdb";
        File extractedFile = new File(extractedPath);

        if (!archiveFile.exists())
        {
            System.out.println("Starting data download (80MB)...");
            FileUtils.copyURLToFile(new URL(DATA_URL), archiveFile);
            System.out.println("Data (.tar.gz file) downloaded to " + archiveFile.getAbsolutePath());
            //Extract tar.gz file to output directory
            DataUtilities.extractTarGz(archizePath, DATA_PATH);
        } else
        {
            //Assume if archive (.tar.gz) exists, then data has already been extracted
            System.out.println("Data (.tar.gz file) already exists at " + archiveFile.getAbsolutePath());
            if (!extractedFile.exists())
            {
                //Extract tar.gz file to output directory
                DataUtilities.extractTarGz(archizePath, DATA_PATH);
            } else
            {
                System.out.println("Data (extracted) already exists at " + extractedFile.getAbsolutePath());
            }
        }
    }


}

/**
 * Convolutional Neural Networks for Sentence Classification - https://arxiv.org/abs/1408.5882
 * <p>
 * Specifically, this is the 'static' model from there
 *
 * @author Alex Black
 */
public class CnnSentenceClassificationExample
{

    /**
     * Data URL for downloading
     */
    public static final String DATA_URL = "http://ai.stanford.edu/~amaas/data/sentiment/aclImdb_v1.tar.gz";
    /**
     * Location to save and extract the training/testing data
     */
    public static final String DATA_PATH = FilenameUtils.concat(System.getProperty("java.io.tmpdir"), "dl4j_w2vSentiment/");
    /**
     * Location (local file system) for the Google News vectors. Set this manually.
     */
    public static final String WORD_VECTORS_PATH = "/PATH/TO/YOUR/VECTORS/GoogleNews-vectors-negative300.bin.gz";

    public static void main(String[] args) throws Exception
    {
        if (WORD_VECTORS_PATH.startsWith("/PATH/TO/YOUR/VECTORS/"))
        {
            throw new RuntimeException("Please set the WORD_VECTORS_PATH before running this example");
        }

        //Download and extract data
        Word2VecSentimentRNN.downloadData();

        //Basic configuration
        int batchSize = 32;
        int vectorSize = 300;               //Size of the word vectors. 300 in the Google News model
        int nEpochs = 1;                    //Number of epochs (full passes of training data) to train on
        int truncateReviewsToLength = 256;  //Truncate reviews with length (# words) greater than this

        int cnnLayerFeatureMaps = 100;      //Number of feature maps / channels / depth for each CNN layer
        PoolingType globalPoolingType = PoolingType.MAX;
        Random rng = new Random(12345); //For shuffling repeatability

        //Set up the network configuration. Note that we have multiple convolution layers, each wih filter
        //widths of 3, 4 and 5 as per Kim (2014) paper.

        Nd4j.getMemoryManager().setAutoGcWindow(5000);

        ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
                .weightInit(WeightInit.RELU)
                .activation(Activation.LEAKYRELU)
                .updater(new Adam(0.01))
                .convolutionMode(ConvolutionMode.Same)      //This is important so we can 'stack' the results later
                .l2(0.0001)
                .graphBuilder()
                .addInputs("input")
                .addLayer("cnn3", new ConvolutionLayer.Builder()
                        .kernelSize(3, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                .addLayer("cnn4", new ConvolutionLayer.Builder()
                        .kernelSize(4, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                .addLayer("cnn5", new ConvolutionLayer.Builder()
                        .kernelSize(5, vectorSize)
                        .stride(1, vectorSize)
                        .nOut(cnnLayerFeatureMaps)
                        .build(), "input")
                //MergeVertex performs depth concatenation on activations: 3x[minibatch,100,length,300] to 1x[minibatch,300,length,300]
                .addVertex("merge", new MergeVertex(), "cnn3", "cnn4", "cnn5")
                //Global pooling: pool over x/y locations (dimensions 2 and 3): Activations [minibatch,300,length,300] to [minibatch, 300]
                .addLayer("globalPool", new GlobalPoolingLayer.Builder()
                        .poolingType(globalPoolingType)
                        .dropOut(0.5)
                        .build(), "merge")
                .addLayer("out", new OutputLayer.Builder()
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nOut(2)    //2 classes: positive or negative
                        .build(), "globalPool")
                .setOutputs("out")
                //Input has shape [minibatch, channels=1, length=1 to 256, 300]
                .setInputTypes(InputType.convolutional(truncateReviewsToLength, vectorSize, 1))
                .build();

        ComputationGraph net = new ComputationGraph(config);
        net.init();

        System.out.println("Number of parameters by layer:");
        for (Layer l : net.getLayers())
        {
            System.out.println("\t" + l.conf().getLayer().getLayerName() + "\t" + l.numParams());
        }

        //Load word vectors and get the DataSetIterators for training and testing
        System.out.println("Loading word vectors and creating DataSetIterators");
        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(new File(WORD_VECTORS_PATH));
        DataSetIterator trainIter = getDataSetIterator(true, wordVectors, batchSize, truncateReviewsToLength, rng);
        DataSetIterator testIter = getDataSetIterator(false, wordVectors, batchSize, truncateReviewsToLength, rng);

        System.out.println("Starting training");
        net.setListeners(new ScoreIterationListener(100), new EvaluativeListener(testIter, 1, InvocationType.EPOCH_END));
        net.fit(trainIter, nEpochs);


        //After training: load a single sentence and generate a prediction
        String pathFirstNegativeFile = FilenameUtils.concat(DATA_PATH, "aclImdb/test/neg/0_2.txt");
        String contentsFirstNegative = FileUtils.readFileToString(new File(pathFirstNegativeFile));
        INDArray featuresFirstNegative = ((CnnSentenceDataSetIterator) testIter).loadSingleSentence(contentsFirstNegative);

        INDArray predictionsFirstNegative = net.outputSingle(featuresFirstNegative);
        List<String> labels = testIter.getLabels();

        System.out.println("\n\nPredictions for first negative review:");
        for (int i = 0; i < labels.size(); i++)
        {
            System.out.println("P(" + labels.get(i) + ") = " + predictionsFirstNegative.getDouble(i));
        }
    }


    private static DataSetIterator getDataSetIterator(boolean isTraining, WordVectors wordVectors, int minibatchSize,
                                                      int maxSentenceLength, Random rng)
    {
        String path = FilenameUtils.concat(DATA_PATH, (isTraining ? "aclImdb/train/" : "aclImdb/test/"));
        String positiveBaseDir = FilenameUtils.concat(path, "pos");
        String negativeBaseDir = FilenameUtils.concat(path, "neg");

        File filePositive = new File(positiveBaseDir);
        File fileNegative = new File(negativeBaseDir);

        Map<String, List<File>> reviewFilesMap = new HashMap<>();
        reviewFilesMap.put("Positive", Arrays.asList(filePositive.listFiles()));
        reviewFilesMap.put("Negative", Arrays.asList(fileNegative.listFiles()));

        LabeledSentenceProvider sentenceProvider = new FileLabeledSentenceProvider(reviewFilesMap, rng);

        return new CnnSentenceDataSetIterator.Builder()
                .sentenceProvider(sentenceProvider)
                .wordVectors(wordVectors)
                .minibatchSize(minibatchSize)
                .maxSentenceLength(maxSentenceLength)
                .useNormalizedWordVectors(false)
                .build();
    }
}
