package pl.hws.phptester.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import pl.hws.phptester.entities.VersionEntity;
import pl.hws.phptester.helpers.SceneHelper;
import pl.hws.phptester.services.VersionsService;

public class HomeController extends AbstractController {
    @FXML
    private TableView versionsTable;

    private final ObservableList versionsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

    @Override
    public void initialized() {
        VersionsService versionsService = VersionsService.getInstance();

        versionsTable.setItems(versionsList);
        versionsTable.setColumnResizePolicy((param) -> true );

        List<VersionEntity> versions = versionsService.getVersionsFromFile();

        if (versions == null) {
            SceneHelper.showLoader(contentPane, "Loading versions");

            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.submit(() -> {
                List<VersionEntity> fetchedVersions = versionsService.fetchVersions();

                versionsList.addAll(fetchedVersions);

                SceneHelper.hideLoader(contentPane);
            });

            executor.shutdown();
        } else {
            versionsList.addAll(versions);
        }
    }
}