package pl.hws.phptester.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import pl.hws.phptester.entities.CommandResultEntity;

public class CommandsHelper {
    public static CommandResultEntity execute(String command, Path directory, SimpleStringProperty logs) {
        Process process;

        try {
            ProcessBuilder ps = new ProcessBuilder(command);

            ps.directory(directory.toFile());
            ps.redirectErrorStream(true);

            process = ps.start();

            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Failed to execute command");
            System.out.println(ex.getLocalizedMessage());

            return null;
        }

        try (InputStream in = process.getInputStream();
                InputStream inErr = process.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(inErr));) {
            String line;
            String content = "";

            while ((line = reader.readLine()) != null) {
                String currentLIne = line;

                Platform.runLater(() -> {
                    logs.set(currentLIne);
                });

                content += line + "\n";
            }

            while ((line = errorReader.readLine()) != null) {
                String currentLIne = line;

                Platform.runLater(() -> {
                    logs.set(currentLIne);
                });

                content += line + "\n";
            }

            CommandResultEntity result = new CommandResultEntity();
            result.setCode(process.exitValue());
            result.setContent(content);

            return result;
        } catch (IOException ex) {
            System.out.println("Failed to fetch command result");
            System.out.println(ex.getLocalizedMessage());

            return null;
        }
    }
}
