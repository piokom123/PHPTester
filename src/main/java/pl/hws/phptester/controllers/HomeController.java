package pl.hws.phptester.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import pl.hws.phptester.entities.VersionEntity;
import pl.hws.phptester.helpers.SceneHelper;
import pl.hws.phptester.services.VersionsService;

public class HomeController extends AbstractController {
    @FXML
    private TableView versionsTable;

    private final ObservableList versionsList = FXCollections.observableArrayList();
    private final VersionsService versionsService = VersionsService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @Override
    public void initialized() {
        versionsTable.setItems(versionsList);
        versionsTable.setColumnResizePolicy((param) -> true );
        versionsTable.getSelectionModel().setSelectionMode(
            SelectionMode.MULTIPLE
        );

        SceneHelper.showLoader(contentPane, "Preparing program");

       ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                SceneHelper.showLoader(contentPane, "Preparing data folder");

                versionsService.prepareDataFolder();

                SceneHelper.showLoader(contentPane, "Loading versions");

                List<VersionEntity> versions = versionsService.getVersionsFromFile();

                if (versions == null) {
                    SceneHelper.showLoader(contentPane, "Fetching versions from php.net");

                    versionsService.fetchVersions();

                    versions = versionsService.getVersionsFromFile();
                }

                versionsList.addAll(versions);

                SceneHelper.hideLoader(contentPane);
            } catch (Throwable ex) {
                SceneHelper.showErrorMessage(contentPane, "Failed to prepare program", ex.getMessage());

                Platform.exit();
                System.exit(1);
            }
        });

        executor.shutdown();
    }

    @FXML
    private void reloadVersions() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                SceneHelper.showLoader(contentPane, "Reloading versions");

                List<VersionEntity> versions = versionsService.getVersionsFromFile();

                if (versions == null) {
                    SceneHelper.showLoader(contentPane, "Fetching versions from php.net");

                    versionsService.fetchVersions();

                    versions = versionsService.getVersionsFromFile();
                }

                versionsList.clear();

                versionsList.addAll(versions);

                SceneHelper.hideLoader(contentPane);
            } catch (Throwable ex) {
                SceneHelper.showErrorMessage(contentPane, "Failed to reload versions", ex.getMessage());
            }
        });

        executor.shutdown();
    }

    @FXML
    private void downloadSelectedVersions() {
        if (versionsTable.getSelectionModel().getSelectedItems().isEmpty()) {
            SceneHelper.showErrorMessage(contentPane, "Select something first", "You have to select at least one version to download");

            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            try {
                SceneHelper.showLoader(contentPane, "Downloading");

                for (Object versionObject : versionsTable.getSelectionModel().getSelectedItems()) {
                    VersionEntity version = (VersionEntity) versionObject;

                    SceneHelper.showLoader(contentPane, "Downloading PHP " + version.getVersion());

                    versionsService.downloadVersion(version);

                    SceneHelper.showLoader(contentPane, "Unpacking PHP " + version.getVersion());

                    versionsService.unpackVersion(version);
                }

                reloadVersions();

                SceneHelper.hideLoader(contentPane);
            } catch (Throwable ex) {
                SceneHelper.showErrorMessage(contentPane, "Failed to download versions", ex.getMessage());
            }
        });

        executor.shutdown();
    }
}