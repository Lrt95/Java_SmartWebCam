package fxmlController;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.tensorflow.Tensor;
import saveImage.SaveImage;
import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ApplicationController {

    @FXML
    public Text textFolder;

    @FXML
    private Text textPath;

    @FXML
    private Text textObject;

    @FXML
    private Text textIndex;

    @FXML
    private Text textProbability;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField objectsName;

    @FXML
    private Spinner percentage;

    @FXML
    private Spinner time;

    @FXML
    private Button button;

    private ImageDescription imageDescription;

    private Stage owner;
    private ArrayList<String> allLabels;
    private byte[] graphDef;

    public void setOwner(Stage value) {
        this.owner = value;
    }

    public void setAllLabels(ArrayList<String> value) {
        this.allLabels = value;
    }

    public void setGraphDef(byte[] value) {
        this.graphDef = value;
    }

    /**
     * set description on application after the tensor result
     * @param imageDescription of the tensor result
     */
    public void setDescription(ImageDescription imageDescription){
        this.imageDescription = imageDescription;
        setTextObject(this.imageDescription.getLabel());
        setTextIndex(this.imageDescription.getIndex());
        setTextProbability(this.imageDescription.getProbability());
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
        Image image = new Image("file:\\" + path);
        this.imageView.setImage(image);
    }

    @FXML
    private String getDescription() {
        return this.objectsName.getText();
    }

    @FXML
    private Integer getPercentage() {
        return Integer.parseInt(this.percentage.getValue().toString());
    }

    @FXML
    private Integer getTime() {
        return Integer.parseInt(this.time.getValue().toString());
    }

    @FXML
    private void handleButtonSave(ActionEvent event) {
        SaveImage saveImage = new SaveImage(getPercentage(), getDescription(), this.textFolder.getText());
        saveImage.save(this.imageDescription);
    }
    /**
     * EventHandler of the button "Select Picture"
     * <p>
     * Select a file and get it to TensorFlow and the data.
     * @param event The event given by the sender
     * @throws IOException if file not exists
     */
    @FXML
    private void selectPictureButtonClick(ActionEvent event) throws IOException {
        File file = openFile();
        if (file != null) {
            TensorFlowUtils tensorFlowUtils = new TensorFlowUtils();

            Tensor<Float> tensor = tensorFlowUtils.executeModelFromByteArray(
                    this.graphDef,
                    tensorFlowUtils.byteBufferToTensor(Utils.readFileToBytes(file.getPath()))
            );

            setDescription(tensorFlowUtils.getDescription(file.getPath(), tensor, this.allLabels));
            setTextPath(file.getPath());
            setImagePath(file.getPath());
        }
    }

    /**
     * Select a file with a chooser window
     * @return The selected file
     */
    private File openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select a picture to open...");
        String sourceFolder = System.getProperty("user.dir") + "/src/main/resources/images";
        //System.out.println("!!!!!!!!" + sourceFolder);
        chooser.setInitialDirectory(new File(sourceFolder));
        FileChooser.ExtensionFilter fileExtensions = new FileChooser.ExtensionFilter("Pictures", "*.jpg", "*.jpeg");
        chooser.getExtensionFilters().add(fileExtensions);
        return chooser.showOpenDialog(this.owner);
    }

    /**
     * EventHandler of the button "Select Save Folder"
     * <p>
     * Select a folder to save the picture.
     * @param event The event given by the sender
     */
    @FXML
    private void selectSaveFolderButtonClick(ActionEvent event) {
        File file = openDirectory();
        if (file != null) {
            System.out.println("folder selected: " + file.getPath());
            textFolder.setText(file.getPath());
        }
    }

    /**
     * Select a folder with a chooser window
     * @return The selected folder
     */
    private File openDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a folder to save image...");
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        return chooser.showDialog(this.owner);
    }
}