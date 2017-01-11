package pl.hws.phptester.services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import pl.hws.phptester.connector.Connector;
import pl.hws.phptester.entities.VersionEntity;

public class VersionsService {
    private static final VersionsService INSTANCE = new VersionsService();

    private List<VersionEntity> versions = null;

    private VersionsService() {
        
    }

    public static VersionsService getInstance() {
        return INSTANCE;
    }

    public List<VersionEntity> getVersionsFromFile() {
        if (versions != null) {
            return versions;
        }

        Path path = Paths.get("versions.data");

        if (!Files.exists(path)) {
            return null;
        }

        try {
            FileInputStream inputStream = new FileInputStream("versions.data");

            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            versions = (List) objectInputStream.readObject();
        } catch (IOException | SecurityException | ClassNotFoundException ex) {
            System.err.println(ex.getLocalizedMessage());

            throw new RuntimeException(ex);
        }

        return versions;
    }

    public List<VersionEntity> fetchVersions() {
        try (Connector connector = new Connector()) {
            PHPNetService phpNetService = new PHPNetService(connector);

            versions = phpNetService.getAllVersions();
        }

        try {
            FileOutputStream outputStream = new FileOutputStream("versions.data");

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(versions);
        } catch (IOException | SecurityException ex) {
            System.err.println(ex.getLocalizedMessage());

            throw new RuntimeException(ex);
        }

        return versions;
    }
}
