package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import shared.RegMessageRequest;

public class RegController {
    @FXML
    private TextField loginTF;
    @FXML
    private TextField passTF;
    @FXML
    private TextField nameTF;
    @FXML
    private TextField emailTF;
    @FXML
    private Button regBtn;

    private  AuthController parentsController;

    public void sendRegMessage(ActionEvent actionEvent) {
        String username = loginTF.getText();
        String password = passTF.getText();
        String name = nameTF.getText();
        String email = emailTF.getText();
        parentsController.sendMessage(new RegMessageRequest(username, password, name, email));
        ((Stage)loginTF.getScene().getWindow()).close();
    }

    public void setParentsController(AuthController parentsController) {
        this.parentsController = parentsController;
    }
}
