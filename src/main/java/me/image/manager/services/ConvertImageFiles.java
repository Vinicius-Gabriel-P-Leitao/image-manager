package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ConvertImageFiles {
    private Alert alert;

    public Task<Void> createTaskConvertFiles(Path originPath, String fileType) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<String> extensions = List.of(".png", ".tiff", ".jpg", ".jpeg", ".webp");
                List<File> skippedFiles = new ArrayList<>();

                long totalFiles = Files.list(originPath).filter(Files::isRegularFile).count();
                AtomicLong convertedFiles = new AtomicLong();

                Files.list(originPath).filter(Files::isRegularFile).forEach(file -> {
                    try {
                        String fileName = file.getFileName().toString();
                        boolean isValidFile = extensions.stream().anyMatch(fileName::endsWith);

                        if (!isValidFile || fileName.endsWith(fileType)) {
                            skippedFiles.add(file.toFile());
                            return;
                        }

                        BufferedImage bufferedImage = ImageIO.read(file.toFile());

                        if (bufferedImage == null) {
                            skippedFiles.add(file.toFile());
                            return;
                        }

                        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                        File outputFile = new File(file.getParent().toFile(), fileNameWithoutExtension + fileType);
                        String extension = fileType.startsWith(".") ? fileType.substring(1) : fileType;

                        boolean isWritten = ImageIO.write(bufferedImage, extension, outputFile);
                        if (!isWritten) {
                            skippedFiles.add(file.toFile());
                            return;
                        }

                        if (outputFile.exists()) {
                            convertedFiles.getAndIncrement();
                            updateProgress(convertedFiles.get(), totalFiles);

                            Desktop.getDesktop().moveToTrash(file.toFile());
                        } else {
                            skippedFiles.add(file.toFile());
                        }
                    } catch (IOException exception) {
                        skippedFiles.add(file.toFile());
                    }
                });

                if (!skippedFiles.isEmpty()) Platform.runLater(() -> {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro ao converter arquivos");
                    alert.setHeaderText("Erro ao converter arquivos!");
                    alert.setContentText(String.format("Os arquivos não podem ser convertidos: %s ...", skippedFiles.stream().limit(5).toList()));
                    alert.showAndWait();
                });
                return null;
            }
        };
    }
}
