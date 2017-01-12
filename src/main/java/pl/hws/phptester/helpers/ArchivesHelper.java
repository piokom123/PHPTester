package pl.hws.phptester.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
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

            while ((entry = tarInputStream.getNextEntry()) != null) {
                Path currentPath = destinationFolder.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectory(currentPath, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
                } else {
                    byte[] bytes = new byte[1024];

                    try (WritableByteChannel wbc = Files.newByteChannel(currentPath, EnumSet.of(CREATE, APPEND));) {
                        while ((tarInputStream.read(bytes, 0, 1024)) > -1) {
                            wbc.write(ByteBuffer.wrap(bytes));
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
