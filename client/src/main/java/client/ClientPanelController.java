package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.extern.java.Log;
import shared.FileInfo;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Log
public class ClientPanelController implements Initializable {
    @FXML
    private TableView<FileInfo> filesTable;
    @FXML
    private ComboBox<String> disksBox;
    @FXML
    private TextField pathField;

    private Path currentClientDir;

    private MainController parentCtrl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PanelCreater.constructTableView(filesTable);

        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);
        updateList(Paths.get("."));

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = currentClientDir.resolve(getSelectedFilename());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });

    }

    public void updateList(Path path) {
            currentClientDir = path;
            Platform.runLater(() -> {
                try {
                    pathField.setText(path.normalize().toAbsolutePath().toString());
                    filesTable.getItems().clear();
                    filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
                    filesTable.sort();
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "По какой-то причине не удалось обновить список файлов", ButtonType.OK);
                    alert.showAndWait();
                }
            });
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        goToUpperFolder();
        updateList(currentClientDir);
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
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

    public Path getCurrentClientDir() {
        return currentClientDir;
    }

    public void deleteFile(String filename) {
        Path path = Paths.get(pathField.getText()).resolve(filename);
        try {
            if (Files.isDirectory(path)){
                Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        log.info("Delete file: " + file.normalize().toAbsolutePath().toString());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        log.info("Delete directory: " + dir.normalize().toAbsolutePath().toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnCreateDir(ActionEvent actionEvent) {
        String filename = parentCtrl.getNameNewDir();
        if (Files.exists(currentClientDir.resolve(filename))){
            for (int i = 1;; i++) {
                String temp = "(" + i + ")";
                if (!Files.exists(currentClientDir.resolve(filename + temp))) {
                    filename = filename + temp;
                    break;
                }
            }
        }
        try {
            Files.createDirectory(currentClientDir.resolve(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateList(currentClientDir);
    }

    public void setParentController(MainController parentCtrl) {
        this.parentCtrl = parentCtrl;
    }

    public void createDir(String filename) {
        if (Files.exists(currentClientDir.resolve(filename))){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно создать папку с таким именем", ButtonType.OK);
            alert.showAndWait();
        }
        try {
            Files.createDirectory(currentClientDir.resolve(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToPath(String fileName) {
        currentClientDir  = currentClientDir.resolve(fileName);
    }

    public void goToUpperFolder() {
        Path upperPath = currentClientDir.toAbsolutePath().getParent();
        if (upperPath != null) {
            currentClientDir = upperPath;
        }
    }

    public void renameFile(String fileName) {
        TextInputDialog nfnDialog = new TextInputDialog();
        nfnDialog.setTitle("Переименование: " + fileName);
        nfnDialog.setHeaderText("Новое имя файла");
        nfnDialog.setContentText("Имя: ");
        Optional<String> result = nfnDialog.showAndWait();
        Path source = currentClientDir.resolve(fileName);
        if (result.isPresent()) {
            Path target = source.resolveSibling(result.get());
            try {
                Files.move(source, target);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
