package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shared.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private VBox clientPanel, serverPanel;

    private ClientPanelController clientController;
    private ServerPanelController serverController;

    private final String NAME_NEW_DIR = "New folder";

    private Network network;
    private AuthController authController;

    public void setAuthController(AuthController authController) {
        this.authController = authController;
    }

    public AuthController getAuthController() {
        return authController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientController = (ClientPanelController) clientPanel.getProperties().get("ctrl");
        serverController = (ServerPanelController) serverPanel.getProperties().get("ctrl");
        serverController.setParentController(this);
        clientController.setParentController(this);
        network = new Network(
                msg -> {
                    if (msg instanceof FilesListResponse) {
                        serverController.updateList(((FilesListResponse) msg).getFiles());
                    }
                    if (msg instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage) msg;
                        handleFileMessage(fileMessage);
                        clientController.updateList(clientController.getCurrentClientDir());
                    }
                    if (msg instanceof ControlCommand){
                        ControlCommand controlCommand = (ControlCommand) msg;
                        switch (controlCommand.getCommand()){
                            case CREATE_FOLDER:
                                clientController.createDir(controlCommand.getFileName());
                                break;
                            case OPEN_FOLDER:
                                clientController.goToPath(controlCommand.getFileName());
                                break;
                            case GO_TO_UPPER_FOLDER:
                                clientController.goToUpperFolder();
                                clientController.updateList(clientController.getCurrentClientDir());
                                break;
                        }
                    }
                    if (msg instanceof AuthMessageResponse) {
                        AuthMessageResponse message = (AuthMessageResponse) msg;
                        if (message.isStatus()){
                            sendMessage(new FilesListRequest());
                            Platform.runLater(()->{
                                ((Stage)authController.getInfoLbl().getScene().getWindow()).close();
                                ((Stage)clientPanel.getScene().getWindow()).show();
                            });

                            clientPanel.getScene().getWindow().requestFocus();
                        } else {
                            Platform.runLater(()->authController.getInfoLbl().setText("Incorrect login or password"));
                        }
                    }
                }
        );

    }

    public String getNameNewDir() {
        return NAME_NEW_DIR;
    }

    public void copyBtnAction(ActionEvent actionEvent) {

        if (clientController.getSelectedFilename() == null && serverController.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (clientController.getSelectedFilename() != null) {
            Path path = Paths.get(clientController.getCurrentPath(), clientController.getSelectedFilename());
            toServer(path);
            network.sendMessage(new FilesListRequest());
        }
        if (serverController.getSelectedFilename() != null) {
            fromServer(serverController.getSelectedFilename());
        }
    }

    private void fromServer(String filename) {
        network.sendMessage(new FileRequest(filename));
    }

    private void toServer(Path file) {
        if (Files.isDirectory(file)){
            sendDirectory(file);
        } else  {
            sendFile(file);
        }
        network.sendMessage(new FilesListRequest());
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    private void handleFileMessage(FileMessage fileMessage) throws IOException {
        Path path = clientController.getCurrentClientDir().resolve(fileMessage.getName());
        Files.write(
                path,
                fileMessage.getContent(),
                StandardOpenOption.CREATE
        );
    }

    public void sendMessage(AbstractCommand abstractCommand) {
        network.sendMessage(abstractCommand);
    }

    public void deleteFile(ActionEvent actionEvent) {
        if (clientController.getSelectedFilename() != null) {
            clientController.deleteFile(clientController.getSelectedFilename());
            clientController.updateList(Paths.get(clientController.getCurrentPath()));
        }
        if (serverController.getSelectedFilename() != null) {
            serverController.deleteFile(serverController.getSelectedFilename());
            network.sendMessage(new FilesListRequest());
        }
    }

    private void sendFile(Path file){
        FileMessage fileMessage = null;
        try {
            fileMessage = new FileMessage(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        network.sendMessage(fileMessage);
    }

    private void sendDirectory(Path dir){
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    sendFile(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String nameDir = dir.getFileName().toString();
                    network.sendMessage(new ControlCommand(ControlCommand.Type.CREATE_FOLDER, nameDir));
                    network.sendMessage(new ControlCommand(ControlCommand.Type.OPEN_FOLDER, nameDir));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    network.sendMessage(new ControlCommand(ControlCommand.Type.GO_TO_UPPER_FOLDER, dir.getFileName().toString()));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renameBtnAction(ActionEvent actionEvent) {
        if (clientController.getSelectedFilename() == null && serverController.getSelectedFilename() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ни один файл не был выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (clientController.getSelectedFilename() != null) {
            clientController.renameFile(clientController.getSelectedFilename());
            clientController.updateList(clientController.getCurrentClientDir());
        }
        if (serverController.getSelectedFilename() != null) {
            serverController.renameFile(serverController.getSelectedFilename());
            network.sendMessage(new FilesListRequest());
        }
    }
}
