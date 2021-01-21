package fxmlController;

import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.controlsfx.control.ToggleSwitch;
import org.tensorflow.Tensor;

import saveImage.SaveImage;

import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

public class ApplicationController implements Initializable {
    @FXML
    private ToggleSwitch switchWebCam;
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
    private TextField textFieldPictureName;
    @FXML
    private Slider sliderPercentage;
    @FXML
    private Text textPercentage;
    @FXML
    private Spinner<Integer> spinnerTime;

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

    private final BooleanProperty disabledWebCam;

    public boolean getDisabledWebCam() {
        return this.disabledWebCam.get();
    }

    public void setDisabledWebCam(boolean value) { this.disabledWebCam.set(value); }

    public BooleanProperty disabledWebCamProperty() {
        return this.disabledWebCam;
    }

    private final BooleanProperty disableSave;

    public boolean getDisableSave() {
        return disableSave.get();
    }

    public void setDisableSave(boolean value) {
        disableSave.set(value);
    }

    public BooleanProperty disableSaveProperty() {
        return disableSave;
    }

    private final String OS;
    private Stage owner;
    private ArrayList<String> allLabels;
    private byte[] graphDef;
    private ImageDescription imageDescription;

    private int percentage;

    public void setOwner(Stage value) {
        this.owner = value;
    }

    public void setAllLabels(ArrayList<String> value) {
        this.allLabels = value;
    }

    public void setGraphDef(byte[] value) {
        this.graphDef = value;
    }

    private String getPictureName() {
        return this.textFieldPictureName.getText();
    }

    private int getSpinnerTime() {
        return Integer.parseInt(this.spinnerTime.getValue().toString());
    }

    /**
     * ApplicationController constructor
     */
    public ApplicationController() {
        this.OS = System.getProperty("os.name").toLowerCase();
        this.folderSave = new SimpleStringProperty(null);
        this.disableSave = new SimpleBooleanProperty(true);
        this.disabledWebCam = new SimpleBooleanProperty(true);
    }

    /**
     * Call after the controller was created.
     * @param url Url
     * @param resources Resources
     */
    public void initialize(URL url, ResourceBundle resources) {
        sliderPercentage.valueProperty().addListener((observable, oldValue, newValue) -> {
            percentage = Integer.parseInt(newValue.toString().split("\\.")[0]);
            textPercentage.setText(percentage + " %");
        });
    }

    /**
     * Remove the double spaces in the textField of PictureName
     */
    private void checkPictureName() {
        this.textFieldPictureName.setText(this.textFieldPictureName.getText().replaceAll(" ", " ").trim());
    }

    /**
     * Check if the user can save the image.
     * Need a folder save, a loaded picture and a picture name.
     */
    private void checkCanSave() {
        setDisableSave(
            folderSave.getValue() == null ||
            getPictureName() == null ||
            getPictureName().length() == 0 ||
            imageDescription == null
        );
    }

    /**
     * set description on application after the tensor result
     * @param imageDescription of the tensor result
     */
    private void setImageDescription(ImageDescription imageDescription){
        this.imageDescription = imageDescription;
        this.textObject.setText(imageDescription != null ? "Object: " + this.imageDescription.getLabel() : "Object: ");
        this.textIndex.setText(imageDescription != null ? "Index: " + this.imageDescription.getIndex() : "Index: ");
        this.textProbability.setText(imageDescription != null ? "Probability: " + Utils.round(this.imageDescription.getProbability() * 100, 2)+ "%" : "Probability: ");
        this.textPath.setText(imageDescription != null ? "Path: " + this.imageDescription.getPath() : "Path: ");
        String separator = OS.contains("win") ? "/" : "//";
        this.imageView.setImage(imageDescription != null ? new Image("file:" + separator + this.imageDescription.getPath()) : null);
        checkPictureName();
        checkCanSave();
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

    @FXML
    private void onSwitchSelected(MouseEvent event) {
        this.setDisabledWebCam(!getDisabledWebCam());
        this.resetAfterToggle();
    }

    private void resetAfterToggle() {
        this.setImageDescription(null);
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
        }
        checkPictureName();
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

    /**
     * Save the picture to the save folder selected
     * @param event The event raise by the sender
     */
    @FXML
    private void handleButtonSave(ActionEvent event) {
        checkPictureName();
        SaveImage saveImage = new SaveImage(this.percentage, getPictureName(), folderSave.getValue());
        saveImage.save(this.imageDescription);
    }

    /**
     * EventHandler on KeyPress on the TextField of PictureName
     * @param keyEvent The event raise by the sender
     */
    @FXML
    private void pictureNameKeyPressed(KeyEvent keyEvent) {
        checkCanSave();
    }
}