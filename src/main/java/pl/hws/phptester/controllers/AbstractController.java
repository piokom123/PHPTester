package pl.hws.phptester.controllers;

import javafx.scene.layout.StackPane;
import lombok.Setter;

public abstract class AbstractController implements ControllerInterface {
    @Setter
    protected StackPane contentPane;
}
