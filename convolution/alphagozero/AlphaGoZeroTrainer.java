package alphagozero;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.deeplearning4j.nn.graph.ComputationGraph;
class DualResnetModel {

    public static ComputationGraph getModel(int blocks, int numPlanes) {

        DL4JAlphaGoZeroBuilder builder = new DL4JAlphaGoZeroBuilder();
        String input = "in";

        builder.addInputs(input);
        String initBlock = "init";
        String convOut = builder.addConvBatchNormBlock(initBlock, input, numPlanes, true);
        String towerOut = builder.addResidualTower(blocks, convOut);
        String policyOut = builder.addPolicyHead(towerOut, true);
        String valueOut = builder.addValueHead(towerOut, true);
        builder.addOutputs(policyOut, valueOut);

        ComputationGraph model = new ComputationGraph(builder.buildAndReturn());
        model.init();

        return model;
    }
}

public class AlphaGoZeroTrainer {

    private static final Logger log = LoggerFactory.getLogger(AlphaGoZeroTrainer.class);

    public static void main(String[] args) {

        int miniBatchSize = 32;
        int boardSize = 19;

        int numResidualBlocks = 20;
        int numFeaturePlanes = 11;

        log.info("Initializing AGZ model");
        ComputationGraph model = DualResnetModel.getModel(numResidualBlocks, numFeaturePlanes);

        log.info("Create dummy data");
        INDArray input = Nd4j.create(miniBatchSize,numFeaturePlanes, boardSize, boardSize);

        // move prediction has one value for each point on the board (19x19) plus one for passing.
        INDArray policyOutput = Nd4j.create(miniBatchSize, boardSize * boardSize + 1);

        // the value network spits out a value between 0 and 1 to assess how good the current board situation is.
        INDArray valueOutput = Nd4j.create(miniBatchSize, 1);

        log.info("Train AGZ model");
        model.fit(new INDArray[] {input}, new INDArray[] {policyOutput, valueOutput});
    }
}
