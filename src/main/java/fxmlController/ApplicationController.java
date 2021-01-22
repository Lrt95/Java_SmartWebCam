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
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.controlsfx.control.ToggleSwitch;
import org.tensorflow.Tensor;

import utils.ImageDescription;
import utils.TensorFlowUtils;
import utils.Utils;

import saveImage.SaveImage;
import imageFilterManager.ImageFilterManager;

public class ApplicationController implements Initializable {

    @FXML
    private TextField yPosPicture;
    @FXML
    private TextField xPosPicture;
    @FXML
    private ToggleSwitch toggleSwitchWebCam;
    @FXML
    private Text textPath;
    @FXML
    private GridPane gridImage;
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
    @FXML
    private ToggleSwitch toggleApplyFilterColor;
    @FXML
    private ToggleSwitch toggleApplyFilterBorder;
    @FXML
    private ToggleSwitch toggleApplyFilterPicture;
    @FXML
    private ComboBox<String> comboBoxFilter;
    @FXML
    private TextField textFieldColorFilter;

    private final StringProperty folderSave;

    public String getFolderSave() {
        return this.folderSave.getValue();
    }

    public void setFolderSave(String value) {
        this.folderSave.setValue(value);
        checkCanSave();
    }

    public StringProperty folderSaveProperty() {
        return this.folderSave;
    }

    private final BooleanProperty disabledWebCam;

    public boolean getDisabledWebCam() {
        return this.disabledWebCam.getValue();
    }

    public void setDisabledWebCam(boolean value) {
        this.disabledWebCam.setValue(value);
    }

    public BooleanProperty disabledWebCamProperty() {
        return this.disabledWebCam;
    }

    private final BooleanProperty disableSave;

    public boolean getDisableSave() {
        return this.disableSave.getValue();
    }

    public void setDisableSave(boolean value) {
        this.disableSave.setValue(value);
    }

    public BooleanProperty disableSaveProperty() {
        return this.disableSave;
    }

    private final BooleanProperty disableFilterEdition;

    public boolean getDisableFilterEdition() {
        return this.disableFilterEdition.getValue();
    }

    public void setDisableFilterEdition(boolean value) {
        this.disableFilterEdition.setValue(value);
    }

    private BooleanProperty disableFilterEditionProperty() {
        return this.disableFilterEdition;
    }

    public ImageDescription getImageDescription() {
        return imageDescription;
    }

    public HashMap<String, ImageFilterManager> getLabelFilters() {
        return labelFilters;
    }

    private final String OS;
    private Stage owner;
    public ArrayList<String> allLabels;
    private final ArrayList<String> allLabelsSelected;
    private final HashMap<String, ImageFilterManager> labelFilters;
    public byte[] graphDef;
    private ImageDescription imageDescription;
    private GridImageController gridImageController;
    private int percentage;

    public void setOwner(Stage value) {
        this.owner = value;
    }

    public void setAllLabels(ArrayList<String> value) {
        this.allLabels = value;
        fetchAvailableLabels();
        fetchFilters();
    }

    public void setGraphDef(byte[] value) {
        this.graphDef = value;
    }

    public int getSpinnerTime() {
        return Integer.parseInt(this.spinnerTime.getValue().toString()) * 1000;
    }

    public String getOS() {
        return this.OS;
    }

    /**
     * ApplicationController constructor
     */
    public ApplicationController() {
        this.OS = System.getProperty("os.name").toLowerCase();
        this.labelFilters = new HashMap<String, ImageFilterManager>();
        this.folderSave = new SimpleStringProperty(null);
        this.disableSave = new SimpleBooleanProperty(true);
        this.disabledWebCam = new SimpleBooleanProperty(true);
        this.disableFilterEdition = new SimpleBooleanProperty(true);
        this.allLabelsSelected = new ArrayList<>();
    }

    /**
     * Call after the controller was created.
     *
     * @param url       Url
     * @param resources Resources
     */
    public void initialize(URL url, ResourceBundle resources) {
        this.sliderPercentage.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.percentage = Integer.parseInt(newValue.toString().split("\\.")[0]);
            this.textPercentage.setText(this.percentage + " %");
        });
        this.comboBoxLabelsAvailable.getEditor().textProperty().addListener((observable, oldValue, newValue) -> fetchAvailableLabels());
        this.comboBoxLabelsAvailable.valueProperty().addListener((observable, oldValue, newValue) -> {
            addSelectedLabel(newValue);
            checkCanSave();
        });
        this.comboBoxLabelsSelected.valueProperty().addListener((observable, oldValue, newValue) -> {
            removeSelectedLabel(newValue);
            checkCanSave();
        });

        //this.comboBoxFilter.getEditor().textProperty().addListener((observable, oldValue, newValue) -> fetchFilters());
        this.comboBoxFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ImageFilterManager manager;
                if (!this.labelFilters.containsKey(newValue)) {
                    this.labelFilters.put(newValue, new ImageFilterManager(0, 0, 0, 255));
                }
                manager = this.labelFilters.get(newValue);
                setDisableFilterEdition(false);
                this.textFieldColorFilter.setText(manager.getColor());
                this.toggleApplyFilterColor.setSelected(manager.getIsFilterColorApply());
                this.toggleApplyFilterBorder.setSelected(manager.getIsFilterBorderApply());
                this.toggleApplyFilterPicture.setSelected(manager.getIsFilterPictureApply());

            } else {
                setDisableFilterEdition(true);
                this.toggleApplyFilterColor.setSelected(false);
                this.toggleApplyFilterBorder.setSelected(false);
                this.toggleApplyFilterPicture.setSelected(false);
            }
        });
        this.toggleApplyFilterColor.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!getDisableFilterEdition()) {
                ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
                manager.setIsFilterColorApply(!manager.getIsFilterColorApply());
            }
        });
        this.toggleApplyFilterPicture.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!getDisableFilterEdition()) {
                ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
                manager.setIsFilterPictureApply(!manager.getIsFilterPictureApply());
            }
        });
        this.toggleApplyFilterBorder.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!getDisableFilterEdition()) {
                ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
                manager.setIsFilterBorderApply(!manager.getIsFilterBorderApply());
            }
        });
        this.textFieldColorFilter.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (!getDisableFilterEdition()) {
                ImageFilterManager manager = this.labelFilters.get(this.comboBoxFilter.getValue());
                manager.setFromString(newValue);
                if (!manager.getColor().equals(newValue)) {
                    this.textFieldColorFilter.setText(manager.getColor());
                }
            }
        }));

        this.toggleSwitchWebCam.selectedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                onToggleClick();
            } catch (FrameGrabber.Exception ignored) {
            }
        });

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
                this.getFolderSave() == null ||
                        this.allLabelsSelected.size() == 0 ||
                        this.imageDescription == null
        );
    }

    /**
     * set description on application after the tensor result
     *
     * @param imageDescription of the tensor result
     */
    public void setImageDescription(ImageDescription imageDescription) {
        this.imageDescription = imageDescription;
        this.textPath.setText(imageDescription != null ? "Path: " + this.imageDescription.getPath() : "Path: ");
        this.gridImageController.setDescription(this.imageDescription);
        checkCanSave();
    }

    /**
     * EventHandler of the button "Select Picture"
     * <p>
     * Select a file and get it to TensorFlow and the data.
     *
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
     * Call when the WebCam toggle is clicked
     *
     * @throws FrameGrabber.Exception if device not found
     */
    private void onToggleClick() throws FrameGrabber.Exception {
        this.resetImageDescription();
        this.setDisabledWebCam(!this.getDisabledWebCam());
        if (!this.getDisabledWebCam()) {
            this.gridImageController.setCam();
        }
    }

    /**
     * Reset image description
     */
    public void resetImageDescription() {
        this.setImageDescription(null);
    }

    /**
     * Select a file with a chooser window
     *
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
     *
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
     *
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
     *
     * @param event The event raise by the sender
     */
    @FXML
    private void handleButtonSave(ActionEvent event) {
        SaveImage saveImage = new SaveImage(this.percentage, this.allLabelsSelected, folderSave.getValue());
        saveImage.save(this.imageDescription, this.gridImageController.getLastBufferedImage());
    }

    /**
     * Feed the comboBox of the filter labels.
     */
    private void fetchFilters() {
        this.comboBoxFilter.getItems().clear();
        for (String label : this.allLabels) {
            this.comboBoxFilter.getItems().add(label);
        }
        this.comboBoxFilter.getItems().sort(String::compareToIgnoreCase);
    }

    /**
     * Feed the comboBox of the available labels.
     */
    private void fetchAvailableLabels() {
        String filter = this.comboBoxLabelsAvailable.getEditor().getText();
        this.comboBoxLabelsAvailable.getItems().clear();
        for (String label : this.allLabels) {
            if (label.startsWith(filter) && !this.allLabelsSelected.contains(label)) {
                this.comboBoxLabelsAvailable.getItems().add(label);
            }
        }
    }

    /**
     * Add a label in the SelectedLabels List
     *
     * @param label The new label to add
     */
    private void addSelectedLabel(String label) {
        if (label != null && label.length() > 0 && !this.allLabelsSelected.contains(label)) {
            this.allLabelsSelected.add(label);
            fetchSelectedLabels();
            fetchAvailableLabels();
        }
    }

    /**
     * Remove a label from the SelectedLabels List
     *
     * @param label The label to remove
     */
    private void removeSelectedLabel(String label) {
        if (label != null && label.length() > 0) {
            this.allLabelsSelected.remove(label);
            fetchSelectedLabels();
            fetchAvailableLabels();
        }
    }

    /**
     * Feed the comboBox of the selected labels.
     */
    private void fetchSelectedLabels() {
        this.comboBoxLabelsSelected.getItems().clear();
        this.allLabelsSelected.sort(String::compareToIgnoreCase);
        for (String label : this.allLabelsSelected) {
            this.comboBoxLabelsSelected.getItems().add(label);
        }
        this.comboBoxLabelsSelected.setPromptText("Labels selected (" + this.comboBoxLabelsSelected.getItems().size() + ")");
    }
}