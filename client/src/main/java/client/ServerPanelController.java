package client;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import shared.ControlCommand;
import shared.FileInfo;
import shared.FilesListRequest;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ServerPanelController implements Initializable {
    @FXML
    private TableView<FileInfo> filesTable;
    @FXML
    private TextField pathField;

    private final String SEPARATOR = " ! ";
    private MainController parentCtrl;

    public void setParentController(MainController parentCtrl) {
        this.parentCtrl = parentCtrl;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PanelCreater.constructTableView(filesTable);
        pathField.setText(File.separator);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    if (filesTable.getSelectionModel().getSelectedItem().isDirectory()){
                        goToPath(getSelectedFilename());
                        parentCtrl.sendMessage(new FilesListRequest());
                    }
                }
            }
        });
    }

    private void goToPath(String dirName) {
        parentCtrl.sendMessage(new ControlCommand(ControlCommand.Type.OPEN_FOLDER, dirName));
        pathField.setText(getCurrentPath() + dirName + File.separator);
    }

    @FXML
    private void goToUpperFolder(){
        parentCtrl.sendMessage(new ControlCommand(ControlCommand.Type.GO_TO_UPPER_FOLDER, null));
        parentCtrl.sendMessage(new FilesListRequest());
        String[] str = getCurrentPath().split(File.separator);
        pathField.clear();
        StringBuilder address = new StringBuilder();
        address.append(File.separator);
        for (int i = 1; i < str.length - 1; i++) {
            address.append(str[i] + File.separator);
        }
        pathField.setText(address.toString());
    }

    public void updateList(List<FileInfo> files) {
        filesTable.getItems().clear();
        filesTable.getItems().addAll(files);
        filesTable.sort();

    }

    public String getSelectedFilename() {
        if (!filesTable.isFocused()) {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath() {
        return pathField.getText();
    }

    public void deleteFile(String filename) {
        parentCtrl.sendMessage(new ControlCommand(ControlCommand.Type.DELETE_FILE, filename));
        parentCtrl.sendMessage(new FilesListRequest());
    }

    public void btnCreateDir(ActionEvent actionEvent) {
        parentCtrl.sendMessage(new ControlCommand(ControlCommand.Type.CREATE_FOLDER,parentCtrl.getNameNewDir()));
        parentCtrl.sendMessage(new FilesListRequest());
    }

    public void renameFile(String fileName) {
        TextInputDialog nfnDialog = new TextInputDialog();
        nfnDialog.setTitle("Переименование: " + fileName);
        nfnDialog.setHeaderText("Новое имя файла");
        nfnDialog.setContentText("Имя: ");
        Optional<String> result = nfnDialog.showAndWait();
        if (result.isPresent()) {
            parentCtrl.sendMessage(new ControlCommand(ControlCommand.Type.RENAME_FILE, fileName + SEPARATOR + result.get()));
        }
    }
}
