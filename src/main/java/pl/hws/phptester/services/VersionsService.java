package pl.hws.phptester.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import pl.hws.phptester.Context;
import pl.hws.phptester.connector.Connector;
import pl.hws.phptester.connector.ConnectorResponse;
import pl.hws.phptester.entities.VersionEntity;
import pl.hws.phptester.entities.CommandResultEntity;
import pl.hws.phptester.enums.VersionStatusEnum;
import pl.hws.phptester.helpers.ArchivesHelper;
import pl.hws.phptester.helpers.CommandsHelper;
import pl.hws.phptester.entities.ServiceResultEntity;

public class VersionsService {
    private static final VersionsService INSTANCE = new VersionsService();

    private List<VersionEntity> versions = null;
    private final Path dataFolder = Paths.get("data");

    private VersionsService() {
        
    }

    public static VersionsService getInstance() {
        return INSTANCE;
    }

    public List<VersionEntity> getVersionsFromFile() {
        if (versions != null) {
            if (reloadVersionsStatuses()) {
                saveVersions();
            }

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

            if (reloadVersionsStatuses()) {
                saveVersions();
            }
        } catch (IOException | SecurityException | ClassNotFoundException ex) {
            Context.getInstance().showError(ex);

            throw new RuntimeException(ex);
        }

        return versions;
    }

    public Boolean fetchVersions() {
        try (Connector connector = new Connector()) {
            PHPNetService phpNetService = new PHPNetService(connector);

            versions = phpNetService.getAllVersions();
        }

        return saveVersions();
    }

    public Boolean prepareDataFolder() {
        if (Files.exists(dataFolder)) {
            if (!Files.isDirectory(dataFolder)) {
                return false;
            }

            if (!Files.isWritable(dataFolder)) {
                return false;
            }

            return true;
        }

        try {
            Files.createDirectory(dataFolder, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
        } catch (IOException ex) {
            Context.getInstance().showError(ex);

            throw new RuntimeException(ex);
        }

        return true;
    }

    public Boolean downloadVersion(VersionEntity version, SimpleDoubleProperty downloadProgress) {
        Path archivePath = dataFolder.resolve(version.getVersion() + ".tar.gz");

        if (Files.exists(archivePath)) {
            return true;
        }

        try (Connector connector = new Connector()) {
            ConnectorResponse response = connector.performBinaryGET(version.getDownloadLink(), archivePath, downloadProgress);

            if (response == null || response.getByteContent() == null) {
                return false;
            }

            Files.write(archivePath, response.getByteContent());

            return true;
        } catch (IOException ex) {
            Context.getInstance().showError(ex);

            return false;
        }
    }

    public Boolean unpackVersion(VersionEntity version) {
        Path archivePath = dataFolder.resolve(version.getVersion() + ".tar.gz");

        if (!Files.exists(archivePath)) {
            return true;
        }

        Path versionRoot = dataFolder.resolve(version.getVersion());

        if (Files.exists(versionRoot)) {
            try {
                Files.walk(versionRoot)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            } catch (IOException ex) {
                Context.getInstance().showError(ex);

                return false;
            }
        }

        try {
            Files.createDirectory(versionRoot, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
        } catch (IOException ex) {
            Context.getInstance().showError(ex);

            return false;
        }

        return ArchivesHelper.unpack(archivePath, versionRoot);
    }

    public ServiceResultEntity compileVersion(VersionEntity version) {
        Path versionRoot = dataFolder.resolve(version.getVersion());

        if (!Files.exists(versionRoot) || !Files.isDirectory(versionRoot)) {
            return new ServiceResultEntity(false, "No root folder found");
        }

        Path configure = versionRoot.resolve("configure");
        Path shTool = versionRoot.resolve("build/shtool");

        try {
            if (Files.exists(shTool)) {
                Files.setPosixFilePermissions(shTool, PosixFilePermissions.fromString("rwxrwxrwx"));
            } else {
                return new ServiceResultEntity(false, "./build/shtool not found");
            }

            if (Files.exists(configure)) {
                Files.setPosixFilePermissions(configure, PosixFilePermissions.fromString("rwxrwxrwx"));
            } else {
                return new ServiceResultEntity(false, "./configure not found");
            }
        } catch (IOException ex) {
            Context.getInstance().showError(ex);

            return new ServiceResultEntity(false, ex.getLocalizedMessage());
        }

        CommandResultEntity configureResult = CommandsHelper.execute("./configure", versionRoot);

        if (configureResult.getCode() != 0) {
            return new ServiceResultEntity(false, "Failed to configure", configureResult.getContent());
        }

        CommandResultEntity makeResult = CommandsHelper.execute("make", versionRoot);

        if (makeResult.getCode() != 0) {
            return new ServiceResultEntity(false, "Failed to compile", makeResult.getContent());
        }

        return new ServiceResultEntity(true, "done");
    }

    private Boolean reloadVersionsStatuses() {
        if (versions == null || versions.isEmpty()) {
            return false;
        }

        Boolean modified = false;

        for (VersionEntity version : versions) {
            VersionStatusEnum currentStatus = getVersionStatus(version);

            if (!currentStatus.equals(version.getStatus())) {
                version.setStatus(currentStatus);

                modified = true;
            }
        }

        return modified;
    }

    private VersionStatusEnum getVersionStatus(VersionEntity version) {
        Path versionRoot = dataFolder.resolve(version.getVersion());
        Path versionArchive = dataFolder.resolve(version.getVersion() + ".tar.gz");
        Path versionBinary = versionRoot.resolve("php");

        if (Files.exists(versionBinary)) {
            return VersionStatusEnum.COMPILED;
        }

        if (Files.exists(versionRoot) && Files.isDirectory(versionRoot)) {
            return VersionStatusEnum.UNPACKED;
        }

        if (Files.exists(versionArchive)) {
            return VersionStatusEnum.FETCHED;
        }

        return VersionStatusEnum.NOT_FETCHED;
    }

    private Boolean saveVersions() {
        try {
            FileOutputStream outputStream = new FileOutputStream("versions.data");

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

            objectOutputStream.writeObject(versions);
        } catch (IOException | SecurityException ex) {
            Context.getInstance().showError(ex);

            return false;
        }

        return true;
    }
}
