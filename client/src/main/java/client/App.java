package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setLocation(getClass().getResource("/client/main.fxml"));
        primaryStage.setTitle("Cloud storage");
        Scene scene = new Scene(mainLoader.load());
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e->{
            primaryStage.close();
            System.exit(0);
        });

        MainController mainController = mainLoader.getController();
        mainController.setAuthController(createAuthController(mainController));
    }

    private AuthController createAuthController(MainController mainController) {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(getClass().getResource("authWindow.fxml"));
        Stage authWindow = new Stage();
        authWindow.setTitle("Authorization");
        try {
            authWindow.setScene(new Scene(authLoader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        authWindow.setOnCloseRequest(e->{
            authWindow.close();
            System.exit(0);
        });
        AuthController authController = authLoader.getController();
        authController.setParentController(mainController);
        authWindow.show();
        return authController;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
