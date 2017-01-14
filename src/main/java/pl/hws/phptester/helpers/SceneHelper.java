package pl.hws.phptester.helpers;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SceneHelper {
    private static TextArea logs = null;

    public static void appendLog(String log) {
        if (logs == null) {
            return;
        }

        Platform.runLater(() -> {
            if (!logs.isVisible()) {
                logs.setVisible(true);
            }

            logs.appendText(log + "\n");
        });
    }

    public static void showLoader(StackPane contentPane, String message) {
        showLoader(contentPane, message, null);
    }

    public static void showLoader(StackPane contentPane, String message, SimpleDoubleProperty progress) {
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
            box.setPadding(new Insets(0, 50, 0, 50));
            box.setId("loader");

            Label label = new Label(message);
            label.setId("loaderLabel");

            box.getChildren().add(label);

            if (progress != null) {
                ProgressBar progressBar = new ProgressBar();

                progressBar.setProgress(0);
                progressBar.setId("loaderProgress");
                progressBar.progressProperty().bind(progress);

                box.getChildren().add(progressBar);
            }

            logs = new TextArea();
            logs.setEditable(false);
            logs.setWrapText(true);
            logs.setId("loaderLogs");
            logs.setVisible(false);

            logs.setMaxWidth(Double.MAX_VALUE);
            logs.setMaxHeight(Double.MAX_VALUE);

            box.getChildren().add(logs);

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

            logs = null;

            contentPane.getChildren().remove(node2);

            for (Node node : contentPane.getChildren()) {
                node.setDisable(false);
            }
        });
    }

    
    public static void showErrorMessage(StackPane contentPane, String message, String details) {
        showErrorMessage(contentPane, message, details, null);
    }

    public static void showErrorMessage(StackPane contentPane, String message, String details, String expandableText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);

            alert.setTitle("Error");
            alert.setHeaderText(message);
            alert.setContentText(details);

            if (expandableText != null) {
                TextArea textArea = new TextArea(expandableText);
                textArea.setEditable(false);
                textArea.setWrapText(true);

                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(new Label("Full log:"), 0, 0);
                expContent.add(textArea, 0, 1);

                alert.getDialogPane().setExpandableContent(expContent);

                alert.getDialogPane().setPrefSize(700, 500);
            }

            alert.showAndWait();

            hideLoader(contentPane);
        });
    }
}
