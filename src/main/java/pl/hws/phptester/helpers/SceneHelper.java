package pl.hws.phptester.helpers;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SceneHelper {
    public static void showLoader(StackPane contentPane, String message) {
        Platform.runLater(() -> {
            Node labelNode = contentPane.lookup("#loaderLabel");
            if (labelNode != null) {
                ((Label) labelNode).setText(message);

                return;
            }

            ProgressIndicator pi = new ProgressIndicator();

            VBox box = new VBox(pi);
            box.setAlignment(Pos.CENTER);
            box.setFillWidth(true);
            box.setId("loader");

            Label label = new Label(message);
            label.setId("loaderLabel");

            box.getChildren().add(label);

            for (Node node : contentPane.getChildren()) {
                node.setDisable(true);
            }

            contentPane.getChildren().add(box);
        });
    }

    public static void hideLoader(StackPane contentPane) {
        Platform.runLater(() -> {
            Node node2 = contentPane.lookup("#loader");

            if (node2 == null) {
                return;
            }

            contentPane.getChildren().remove(node2);

            for (Node node : contentPane.getChildren()) {
                node.setDisable(false);
            }
        });
    }

    public static void showErrorMessage(StackPane contentPane, String message, String details) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);

            alert.setTitle("Error");
            alert.setHeaderText(message);
            alert.setContentText(details);

            alert.showAndWait();

            hideLoader(contentPane);
        });
    }
}
