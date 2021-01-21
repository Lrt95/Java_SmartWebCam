package fxmlController;

import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.tensorflow.Tensor;

import saveImage.SaveImage;

import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

import javax.imageio.ImageIO;

public class ApplicationController implements Initializable {
    @FXML
    private GridPane gridImage;
    @FXML
    private Text textPath;
    @FXML
    private TextField textFieldPictureName;
    @FXML
    private Slider sliderPercentage;
    @FXML
    private Text textPercentage;
    @FXML
    private Spinner<Integer> spinnerTime;
    @FXML
    private ComboBox<String> comboBoxLabelsAvailable;
    @FXML
    private ComboBox<String> comboBoxLabelsSelected;

    private final Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();

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
    private final ArrayList<String> allLabelsSelected;
    private byte[] graphDef;
    private ImageDescription imageDescription;
    private GridImageController gridImageController;

    private int percentage;

    public void setOwner(Stage value) {
        this.owner = value;
    }

    public void setAllLabels(ArrayList<String> value) {
        value.sort(String::compareToIgnoreCase);
        this.allLabels = value;
        fetchAvailableLabels();
    }

    public void setGraphDef(byte[] value) {
        this.graphDef = value;
    }

    private int getSpinnerTime() {
        return Integer.parseInt(this.spinnerTime.getValue().toString()) * 1000;
    }

    public String getOS(){
        return this.OS;
    }

    /**
     * ApplicationController constructor
     */
    public ApplicationController() {
        this.OS = System.getProperty("os.name").toLowerCase();
        this.folderSave = new SimpleStringProperty(null);
        this.disableSave = new SimpleBooleanProperty(true);
        this.disabledWebCam = new SimpleBooleanProperty(true);
        this.allLabelsSelected = new ArrayList<>();
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
        this.comboBoxLabelsAvailable.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            fetchAvailableLabels();
            checkCanSave();
        });
        this.comboBoxLabelsAvailable.valueProperty().addListener((observable, oldValue, newValue) -> addSelectedLabel(newValue));
        this.comboBoxLabelsSelected.valueProperty().addListener(((observable, oldValue, newValue) -> removeSelectedLabel(newValue)));

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/imagePanel.fxml"));
        try {
            Parent children = loader.load();
            this.gridImage.getChildren().add(children);
            this.gridImageController = loader.<GridImageController>getController();
            this.gridImageController.setOwner(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Check if the user can save the image.
     * Need a folder save, a loaded picture and a picture name.
     */
    private void checkCanSave() {
        setDisableSave(
                folderSave.getValue() == null ||
                this.allLabelsSelected.size() == 0 ||
                imageDescription == null
        );
    }

    /**
     * set description on application after the tensor result
     * @param imageDescription of the tensor result
     */
    private void setImageDescription(ImageDescription imageDescription) {
        this.imageDescription = imageDescription;
        this.textPath.setText(imageDescription != null ? "Path: " + this.imageDescription.getPath() : "Path: ");
        this.gridImageController.setDescription(this.imageDescription);
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
    private void onSwitchSelected(MouseEvent event) throws FrameGrabber.Exception {
        this.resetAfterToggle();
        this.setDisabledWebCam(!this.getDisabledWebCam());
        if (!this.getDisabledWebCam()) {
            this.setCam();
        }
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
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(this.gridImageController.getImageView().getImage(), null);
        SaveImage saveImage = new SaveImage(this.percentage, this.allLabelsSelected, folderSave.getValue());
        saveImage.save(this.imageDescription, bufferedImage);
    }

    private void fetchAvailableLabels() {
        String filter = this.comboBoxLabelsAvailable.getEditor().getText();
        this.comboBoxLabelsAvailable.getItems().clear();
        for (String label : this.allLabels) {
            if (label.startsWith(filter) && !this.allLabelsSelected.contains(label)) {
                this.comboBoxLabelsAvailable.getItems().add(label);
            }
        }
    }

    private void addSelectedLabel(String label) {
        if (label != null && label.length() > 0 && !this.allLabelsSelected.contains(label)) {
            this.allLabelsSelected.add(label);
            fetchSelectedLabels();
            fetchAvailableLabels();
        }
    }

    private void removeSelectedLabel(String label) {
        if (label != null && label.length() > 0) {
            this.allLabelsSelected.remove(label);
            fetchSelectedLabels();
            fetchAvailableLabels();
        }
    }

    private void fetchSelectedLabels() {
        this.comboBoxLabelsSelected.getItems().clear();
        //Collections.sort(this.allLabelsSelected);
        this.allLabelsSelected.sort(String::compareToIgnoreCase);
        for (String label : this.allLabelsSelected) {
            this.comboBoxLabelsSelected.getItems().add(label);
        }
        this.comboBoxLabelsSelected.setPromptText("Labels selected (" + this.comboBoxLabelsSelected.getItems().size() + ")");
    }

    @FXML
    private void setCam() throws FrameGrabber.Exception {
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(2);



            grabber.start();
            Executors.newSingleThreadExecutor().execute(() -> {
                Frame frame;
                while (!this.getDisabledWebCam()) {
                    try {
                        frame = grabber.grabFrame();
                        setFrameToImageView(frame);
                    } catch (FrameGrabber.Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    grabber.stop();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
                this.resetAfterToggle();
            });

    }

    private void setFrameToImageView(Frame frame) {
        try {
            Thread.sleep(this.getSpinnerTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.gridImageController.getImageView().setImage(frameToImage(frame));
    }

    private WritableImage frameToImage(Frame frame) {
        BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);
        try {
            this.setDescriptionCam(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private void setDescriptionCam(BufferedImage bufferedImage) throws IOException {

        byte[] bytes = toByteArray(bufferedImage, "jpg");


        TensorFlowUtils tensorFlowUtils = new TensorFlowUtils();
        Tensor<Float> tensor = tensorFlowUtils.executeModelFromByteArray(
                this.graphDef,
                tensorFlowUtils.byteBufferToTensor(bytes)
        );
        setImageDescription(tensorFlowUtils.getDescription(null, tensor, this.allLabels));
    }

    // convert BufferedImage to byte[]
    public static byte[] toByteArray(BufferedImage bufferedImage, String format)
            throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, format, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }

}