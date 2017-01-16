package pl.hws.phptester.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import pl.hws.phptester.Context;

public class ArchivesHelper {
    public static Boolean unpack(Path archivePath, Path destinationFolder) {
        try {
            InputStream inputStream = Files.newInputStream(archivePath);

            GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(inputStream);
            TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream);

            ArchiveEntry entry;
            String rootFolder = "";

            while ((entry = tarInputStream.getNextEntry()) != null) {
                Path currentPath = destinationFolder.resolve(entry.getName().replace(rootFolder, ""));

                if (entry.isDirectory()) {
                    if (rootFolder.isEmpty() && entry.getName().contains("php-")) {
                        rootFolder = entry.getName();

                        continue;
                    }

                    Files.createDirectory(currentPath, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
                } else {
                    byte[] bytes = new byte[1024];

                    try (OutputStream output = Files.newOutputStream(currentPath);) {
                        Integer readBytes = 0;

                        while ((readBytes = tarInputStream.read(bytes, 0, 1024)) > -1) {
                            output.write(bytes, 0, readBytes);

                            bytes = new byte[1024];
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Context.getInstance().showError("Failed to unpack archive");

            Context.getInstance().showError(ex);
        }

        return true;
    }
}
