package pl.hws.phptester.helpers;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SceneHelper {
    public static void showLoader(StackPane contentPane, String message) {
        ProgressIndicator pi = new ProgressIndicator();
        VBox box = new VBox(pi);
        box.setAlignment(Pos.CENTER);
        box.setFillWidth(true);
        box.setId("loader");

        Label label = new Label(message);

        box.getChildren().add(label);

        for (Node node : contentPane.getChildren()) {
            node.setDisable(true);
        }

        contentPane.getChildren().add(box);
    }

    public static void hideLoader(StackPane contentPane) {
        Node node2 = contentPane.lookup("#loader");

        Platform.runLater(() -> {
            contentPane.getChildren().remove(node2);

            for (Node node : contentPane.getChildren()) {
                node.setDisable(false);
            }
        });
    }
}
