package launcher;

import fxmlController.ApplicationController;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.tensorflow.Tensor;
import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        ArrayList<String> allLabels = Utils.readFile("inception5h/labels.txt");
        byte[] graphDef = Utils.readFileToBytes("inception5h/tensorflow_inception_graph.pb");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/application.fxml"));
        Parent root = loader.load();

        ApplicationController controller = loader.<ApplicationController>getController();
        controller.setOwner(primaryStage);
        controller.setAllLabels(allLabels);
        controller.setGraphDef(graphDef);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
