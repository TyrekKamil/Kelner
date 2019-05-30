package ramo.klevis.ml.vg16;

import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class VG16 {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TrainImageNetVG16.class);
    private static final String TRAINED_PATH_MODEL = TrainImageNetVG16.DATA_PATH + "/model.zip";
    private static ComputationGraph computationGraph;

    public static void main(String[] args) throws IOException {
        new VG16().runOnTestSet();
    }


    public FoodType detectBurger(File file, Double threshold) throws IOException {
        if (computationGraph == null) {
            computationGraph = loadModel();
        }

        computationGraph.init();
        log.info(computationGraph.summary());
        NativeImageLoader loader = new NativeImageLoader(224, 224, 3);
        INDArray image = loader.asMatrix(new FileInputStream(file));
        DataNormalization scaler = new VGG16ImagePreProcessor();
        scaler.transform(image);
        INDArray output = computationGraph.outputSingle(false, image);
        if (output.getDouble(0) > threshold) {
            return FoodType.BURGER;
        }else if(output.getDouble(1) > threshold){
            return FoodType.PIZZA;
        }
        else if(output.getDouble(2) > threshold){
            return FoodType.SALAD;
        }
        else if(output.getDouble(3) > threshold){
            return FoodType.SPAGHETTI;
        }
        else{
            return FoodType.NOT_KNOWN;
        }
    }

    private void runOnTestSet() throws IOException {
        ComputationGraph computationGraph = loadModel();
        File trainData = new File(TrainImageNetVG16.TEST_FOLDER);
        FileSplit test = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, TrainImageNetVG16.RAND_NUM_GEN);
        InputSplit inputSplit = test.sample(TrainImageNetVG16.PATH_FILTER, 100, 0)[0];
        DataSetIterator dataSetIterator = TrainImageNetVG16.getDataSetIterator(inputSplit);
        TrainImageNetVG16.evalOn(computationGraph, dataSetIterator, 1);
    }

    private void runOnDevSet() throws IOException {
        ComputationGraph computationGraph = loadModel();
        File trainData = new File(TrainImageNetVG16.TRAIN_FOLDER);
        FileSplit test = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, TrainImageNetVG16.RAND_NUM_GEN);
        InputSplit inputSplit = test.sample(TrainImageNetVG16.PATH_FILTER, 15, 85)[0];
        DataSetIterator dataSetIterator = TrainImageNetVG16.getDataSetIterator(inputSplit);
        TrainImageNetVG16.evalOn(computationGraph, dataSetIterator, 1);
    }

    public ComputationGraph loadModel() throws IOException {
        computationGraph = ModelSerializer.restoreComputationGraph(new File(TRAINED_PATH_MODEL));
        return computationGraph;
    }

}
