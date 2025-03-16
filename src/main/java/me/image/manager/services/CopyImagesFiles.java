package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import me.image.manager.components.DefaultAlert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CopyImagesFiles {
    public Task<Void> copyFiles(Path originPath, Path destinationPath, ProgressBar progressBar) {

        if (originPath == null || destinationPath == null) {
            Platform.runLater(() -> {
                new DefaultAlert().alert(Alert.AlertType.ERROR, "Os diretórios não podem ser vazios!", "Os diretórios não podem ser vazios!");
            });
            throw new RuntimeException("Os diretórios não podem ser vazios.");
        }

        Platform.runLater(() -> {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });

        try {
            Files.createDirectories(destinationPath);

            if (Files.isRegularFile(originPath)) {
                copySingleFile(originPath, destinationPath);

            } else if (Files.isDirectory(originPath)) {
                Task<Void> copyTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        List<String> extensions = List.of(".png", ".tiff", ".jpg", ".jpeg", ".webp");
                        List<String> skippedFiles = new ArrayList<>();
                        List<String> notValidExtension = new ArrayList<>();

                        Files.list(destinationPath).forEach(sourceFile -> {
                            skippedFiles.add(sourceFile.getFileName().toString());
                        });

                        try (var stream = Files.list(originPath)) {
                            List<Path> filesToCopy = stream.filter(sourceFile -> {
                                String fileName = sourceFile.getFileName().toString();
                                boolean isValidFile = extensions.stream().anyMatch(fileName::endsWith);

                                if (!isValidFile) {
                                    notValidExtension.add(fileName);
                                }

                                return isValidFile;
                            }).toList();

                            int totalFiles = filesToCopy.size();
                            int copiedFiles = 0;

                            for (Path sourceFile : filesToCopy) {
                                Path destinationFile = destinationPath.resolve(sourceFile.getFileName());

                                if (!Files.exists(destinationFile)) {
                                    copySingleFile(sourceFile, destinationFile);
                                }

                                copiedFiles++;
                                updateProgress(copiedFiles, totalFiles);
                            }
                        }

                        Platform.runLater(() -> {
                            if (!notValidExtension.isEmpty()) {
                                new DefaultAlert().alert(Alert.AlertType.WARNING, "Alguns arquivos não podem ser copiados: \n" + notValidExtension, "Arquivos não válidos");
                            }

                            if (!skippedFiles.isEmpty()) {
                                new DefaultAlert().alert(Alert.AlertType.WARNING, "Os seguintes arquivos já existem e foram ignorados: " + skippedFiles.stream().limit(15).toList() + "...", "Arquivos Ignorados");
                            }
                        });

                        return null;
                    }
                };
                progressBar.progressProperty().bind(copyTask.progressProperty());

                Thread copyThread = new Thread(copyTask);
                copyThread.setDaemon(true);
                copyThread.start();

                return copyTask;
            }
        } catch (IOException exception) {
            Platform.runLater(() -> {
                new DefaultAlert().alert(Alert.AlertType.ERROR, "Erro ao listar arquivos: " + exception.getMessage(), "Erro ao listar arquivos");
            });
            throw new RuntimeException("Erro ao listar arquivos: " + exception.getMessage());
        }

        return null;
    }

    private void copySingleFile(Path sourceFile, Path destinationFile) throws IOException {
        if (Files.exists(destinationFile)) {
            return;
        }

        try {
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            if (exception.getMessage().contains("usado")) {
                try {
                    Thread.sleep(1000);
                    Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException | InterruptedException exception1) {
                    Platform.runLater(() -> {
                        new DefaultAlert().alert(Alert.AlertType.ERROR, "Erro ao copiar arquivo: " + exception1.getMessage(), "Erro ao copiar!");
                    });
                    throw new RuntimeException(exception1.getMessage());
                }
            } else {
                throw exception;
            }
        }
    }
}
