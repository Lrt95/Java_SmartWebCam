package fxmlController;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import utils.ImageDescription;
import utils.Utils;

public class GridImageController {
    @FXML
    private Text textProbability;
    @FXML
    private Text textObject;
    @FXML
    private Text textIndex;
    @FXML
    private ImageView imageView;

    private ApplicationController owner;

    public void setOwner(ApplicationController owner) {
        this.owner = owner;
    }

    public void setDescription(ImageDescription imageDescription) {
        this.textObject.setText(imageDescription != null ? "Object: " + imageDescription.getLabel() : "Object: ");
        this.textIndex.setText(imageDescription != null ? "Index: " + imageDescription.getIndex() : "Index: ");
        this.textProbability.setText(imageDescription != null ? "Probability: " + Utils.round(imageDescription.getProbability() * 100, 2) + "%" : "Probability: ");
        String separator = this.owner.getOS().contains("win") ? "/" : "//";
        this.imageView.setImage(imageDescription != null ? new Image("file:" + separator + imageDescription.getPath()) : null);
    }

    public ImageView getImageView() {
        return this.imageView;
    }
}
