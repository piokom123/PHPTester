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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentPane.getChildren().clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));

            ControllerInterface controller = new HomeController();

            controller.setContentPane(currentPane);

            loader.setController(controller);

            currentPane.getChildren().add((Node) loader.load());

            controller.initialized();
        } catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());

            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void closeApplication() {
        Platform.exit();
        System.exit(0);
    }
}