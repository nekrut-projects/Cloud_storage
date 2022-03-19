package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import shared.AuthMessageRequest;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable {
    @FXML
    private TextField loginTF;
    @FXML
    private PasswordField passTF;
    @FXML
    private Button enterBtn;
    @FXML
    private Button regBtn;
    @FXML
    private Label infoLbl;

    private MainController parentController;

    public void setParentController(MainController parentController) {
        this.parentController = parentController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void sendAuthMessage(ActionEvent actionEvent) {
        sendMessage(new AuthMessageRequest(loginTF.getText(), passTF.getText()));
    }

    public void sendMessage(AuthMessageRequest authMessage){
        parentController.sendMessage(authMessage);
    }

    public void showRegForm(ActionEvent actionEvent) {
        FXMLLoader regLoader = new FXMLLoader();
        regLoader.setLocation(getClass().getResource("registration.fxml"));
        Stage regWindow = new Stage();
        try {
            regWindow.setScene(new Scene(regLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        regWindow.initModality(Modality.APPLICATION_MODAL);
        regWindow.initOwner(loginTF.getScene().getWindow());
        ((RegController)regLoader.getController()).setParentsController(this);
        regWindow.show();
    }

    public Label getInfoLbl() {
        return infoLbl;
    }
}
