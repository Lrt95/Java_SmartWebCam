package launcher;

import fxmlController.ApplicationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.tensorflow.Tensor;
import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;


import java.io.IOException;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws IOException {

        String pathImageCheck = "/images/mouse.jpg";

        TensorFlowUtils tensorFlowUtils = new TensorFlowUtils();

        Tensor<Float> tensor = tensorFlowUtils.executeModelFromByteArray(
                Utils.readFileToBytes("inception5h/tensorflow_inception_graph.pb"),
                tensorFlowUtils.byteBufferToTensor(Utils.readFileToBytes("src/main/resources" + pathImageCheck))
        );

        ImageDescription description = tensorFlowUtils.getDescription(tensor, Utils.readFile("inception5h/labels.txt"));

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/application.fxml"));
        Parent root = loader.load();

        ApplicationController controller = loader.<ApplicationController>getController();
        controller.setDescription(description);
        controller.setImagePath(pathImageCheck);
        controller.setTextPath(pathImageCheck);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
