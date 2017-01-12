package pl.hws.phptester.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainController implements Initializable {
    @FXML
    private StackPane currentPane;

    private ControllerInterface currentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startHomeController();
    }

    @FXML
    private void closeApplication() {
        Platform.exit();
        System.exit(0);
    }

    private void startHomeController() {
        currentPane.getChildren().clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));

            currentController = new HomeController();

            currentController.setContentPane(currentPane);

            loader.setController(currentController);

            currentPane.getChildren().add((Node) loader.load());

            currentController.initialized();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());

            throw new RuntimeException(ex);
        }
    }
}