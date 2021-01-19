import org.tensorflow.Tensor;
import utils.Utils;

import java.io.*;

public class Main {

    public static void main(String[] argv) throws IOException {

        TensorFlow tensorFlow = new TensorFlow();

        Tensor<Float> tensor = tensorFlow.executeModelFromByteArray(
                Utils.readFileToBytes("inception5h/tensorflow_inception_graph.pb"),
                tensorFlow.byteBufferToTensor(Utils.readFileToBytes("inception5h/tensorPics/retriever.jpg"))
        );

        TensorFlow.ImageDescription descriptor = tensorFlow.getDescription(tensor, Utils.readFile("inception5h/labels.txt"));

        System.out.println("Index: " + descriptor.getIndex());
        System.out.println("Max: " + Utils.round(descriptor.getProbability() * 100, 2)+ "%");
        System.out.println("Label: " + descriptor.getLabel());
    }
}
