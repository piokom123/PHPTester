package pl.hws.phptester.controllers;

import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

public interface ControllerInterface extends Initializable {
    public void setContentPane(StackPane contentPane);

    public void initialized();
}
