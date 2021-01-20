package fxmlController;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ApplicationController implements Initializable {

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
    private Spinner<Integer> percentage;
    @FXML
    private Spinner<Integer> time;

    private final StringProperty folderSave;
    public String getFolderSave() {
        return folderSave.getValue();
    }
    public void setFolderSave(String value) {
        folderSave.setValue(value);
        checkCanSave();
    }
    public StringProperty folderSaveProperty() {
        return folderSave;
    }

    private final  BooleanProperty disableSave;
    public boolean getDisableSave() {
        return disableSave.get();
    }
    public void    setDisableSave(boolean value) {
        disableSave.set(value);
    }
    public         BooleanProperty disableSaveProperty() {
        return disableSave;
    }

    private Stage owner;
    private ArrayList<String> allLabels;
    private byte[] graphDef;
    private ImageDescription imageDescription;

    public void setOwner(Stage value) {
        this.owner = value;
    }

    public void setAllLabels(ArrayList<String> value) {
        this.allLabels = value;
    }

    public void setGraphDef(byte[] value) {
        this.graphDef = value;
    }

    public ApplicationController() {
        folderSave = new SimpleStringProperty(null);
        disableSave = new SimpleBooleanProperty(true);
    }

    public void initialize(URL url, ResourceBundle resources) {

    }

    private void checkCanSave() {
        setDisableSave(
                folderSave.getValue() == null ||
                imageDescription == null
        );
    }

    /**
     * set description on application after the tensor result
     * @param imageDescription of the tensor result
     */
    private void setImageDescription(ImageDescription imageDescription){
        this.imageDescription = imageDescription;
        this.textObject.setText("Object: " + this.imageDescription.getLabel());
        this.textIndex.setText("Index: " + this.imageDescription.getIndex());
        this.textProbability.setText("Probability: " + Utils.round(this.imageDescription.getProbability() * 100, 2)+ "%");
        this.textPath.setText("Path: " + this.imageDescription.getPath());
        this.imageView.setImage(new Image("file:\\" + this.imageDescription.getPath()));
        checkCanSave();
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
            setImageDescription(tensorFlowUtils.getDescription(file.getPath(), tensor, this.allLabels));
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
            setFolderSave(file.getPath());
            //folderSave.setValue(file.getPath());
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

    @FXML
    private void handleButtonSave(ActionEvent event) {
        SaveImage saveImage = new SaveImage(getPercentage(), getDescription(), folderSave.getValue());
        saveImage.save(this.imageDescription);
    }
}