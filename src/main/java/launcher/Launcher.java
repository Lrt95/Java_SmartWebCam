package launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }



    @Override
    public void start(Stage primaryStage) throws IOException {

//        String pathImageCheck = "/images/jack.jpg";
//
//        TensorFlowUtils tensorFlowUtils = new TensorFlowUtils();
//
//        Tensor<Float> tensor = tensorFlowUtils.executeModelFromByteArray(
//                Utils.readFileToBytes("inception5h/tensorflow_inception_graph.pb"),
//                tensorFlowUtils.byteBufferToTensor(Utils.readFileToBytes("src/main/resources" + pathImageCheck))
//        );
//
//        TensorFlowUtils.ImageDescription descriptor = tensorFlowUtils.getDescription(tensor, Utils.readFile("inception5h/labels.txt"));
//
//        System.out.println("Index: " + descriptor.getIndex());
//        System.out.println("Max: " + Utils.round(descriptor.getProbability() * 100, 2)+ "%");
//        System.out.println("Label: " + descriptor.getLabel());
//
//        primaryStage.setTitle("Smart Webcam");
////        Button btn = new Button();
////        btn.setText("Say 'Hello World'");
////        btn.setOnAction((action) -> {
////            System.out.println("Hello World!");
////        });
//
//        // create a textfield
////        TextField b = new TextField();
//        Label l = new Label(descriptor.getLabel());
//
////        // action event
////        EventHandler<ActionEvent> event = (ActionEvent e) -> {
////            l.setText(b.getText());
////        };
////
////        // when enter is pressed
////        b.setOnAction(event);
//
//        final ImageView imageView = new ImageView();
//        //imageView.setImage(); //Here the image to set
//        Platform.runLater(()->{
//            imageView.setImage(new Image(this.getClass().getResource(pathImageCheck).toString()));
//        });
//
//        imageView.setFitHeight(100);
//        imageView.setFitWidth(100);
//
//        GridPane root = new GridPane();
////        root.getChildren().add(btn);
////        // add textfield
////        root.getChildren().add(b);
//        ColumnConstraints column1 = new ColumnConstraints();
//        column1.setPercentWidth(100);
//        column1.setHalignment(HPos.RIGHT);
//
//
//        RowConstraints row1 = new RowConstraints();
//        row1.setPercentHeight(50);
//        row1.setValignment(VPos.BOTTOM);
//
//        RowConstraints row2 = new RowConstraints();
//        row2.setPercentHeight(50);
//        root.getRowConstraints().addAll(row1, row2);
//
//
//        GridPane.setRowIndex(l, 1);
//        GridPane.setColumnIndex(l, 0);
//        root.getChildren().add(l);
//        GridPane.setRowIndex(imageView, 0);
//        GridPane.setColumnIndex(imageView, 0);
//        root.getChildren().add(imageView);
//        primaryStage.setScene(new Scene(root, 600, 600));
//        primaryStage.show();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/application.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
