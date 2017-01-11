package pl.hws.phptester.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class MainController implements Initializable {
    @FXML
    private AnchorPane currentPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    private void openNext() {
        URL url = getClass().getResource("fxml/home.fxml");

        currentPane.getChildren().clear();

        try {
            currentPane.getChildren().add((Node) FXMLLoader.load(getClass().getResource("/fxml/home.fxml")));
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