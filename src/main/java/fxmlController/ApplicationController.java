package fxmlController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import utils.ImageDescription;
import utils.Utils;

public class ApplicationController {

    @FXML
    private Text textPath;

    @FXML
    private Text textObject;

    @FXML
    private Text textIndex;


    @FXML
    private Text textProbability;

    @FXML
    private ImageView image;

//    @FXML
//    private void handleButtonAction(ActionEvent event) {
//        System.out.println("You clicked me!");
//        description.setText("Hello World!");
//    }

    /**
     * set description on application after the tensor result
     * @param imageDescription of the tensor result
     */
    public void setDescription(ImageDescription imageDescription){
        setTextObject(imageDescription.getLabel());
        setTextIndex(imageDescription.getIndex());
        setTextProbability(imageDescription.getProbability());
    }

    /**
     * set textPath on application for the image check
     * @param path of the image check
     */
    @FXML
    public void setTextPath(String path) {
        textPath.setText("Path: " + path);
    }

    /**
     * set textObject on application after the tensor result
     * @param object of the tensor result
     */
    @FXML
    private void setTextObject(String object) {
        textObject.setText("Object: " + object);
    }


    /**
     * set textIndex on application after the tensor result
     * @param index of the tensor result
     */
    @FXML
    private void setTextIndex(int index) {
        textIndex.setText("Index: " + index);
    }

    /**
     * set textProbability on application after the tensor result
     * @param probability of the tensor result
     */
    @FXML
    private void setTextProbability(float probability) {
        textProbability.setText("Probability: " + Utils.round(probability * 100, 2)+ "%");
    }

    /**
     * set image on application after the tensor result
     * @param path of the image check
     */
    @FXML
    public void setImagePath(String path) {
        this.image.setImage(new Image(this.getClass().getResource(path).toString()));
    }


    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
    }
}