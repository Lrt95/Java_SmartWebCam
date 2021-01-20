package fxmlconroller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

public class ApplicationController {

    public Button button;

    @FXML
    private Label description;

    @FXML
    private Text target;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        description.setText("Hello World!");
    }

    @FXML
    public void setData(String data) {
        target.setText(data);
    }
}