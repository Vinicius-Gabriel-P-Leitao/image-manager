package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import me.image.manager.components.DefaultAlert;

import java.io.IOException;
import java.nio.file.FileSystemException;
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
            if (!Files.exists(destinationPath)) Files.createDirectories(destinationPath);

            if (Files.isRegularFile(originPath)) {
                copySingleFile(originPath, destinationPath);

            } else if (Files.isDirectory(originPath)) {
                return new Task<Void>() {
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

                                if (Files.exists(destinationFile)) {
                                    skippedFiles.add(sourceFile.getFileName().toString());
                                    continue;
                                }

                                copySingleFile(sourceFile, destinationFile);

                                copiedFiles++;
                                updateProgress(copiedFiles, totalFiles);
                            }
                        }

                        Platform.runLater(() -> {
                            if (!notValidExtension.isEmpty()) {
                                new DefaultAlert().alert(Alert.AlertType.WARNING, "Alguns arquivos não podem ser copiados: \n" + notValidExtension, "Arquivos não válidos");
                            }
                        });

                        Platform.runLater(() -> {
                            if (!skippedFiles.isEmpty()) {
                                new DefaultAlert().alert(Alert.AlertType.WARNING, "Os seguintes arquivos já existem e foram ignorados: " + skippedFiles.stream().limit(15).toList() + "...", "Arquivos Ignorados");
                            }
                        });
                        return null;
                    }
                };
            }
        } catch (IOException exception) {
            Platform.runLater(() -> {
                new DefaultAlert().alert(Alert.AlertType.ERROR, "Erro ao listar arquivos: " + exception.getMessage(), "Erro ao listar arquivos");
            });
            throw new RuntimeException("Erro ao listar arquivos: " + exception.getMessage(), exception);
        }

        return null;
    }

    /**
     * <h4>Método para copiar arquivos de forma unitária uma a um</h4>
     *
     * <p>
     * Código recebe o arquivo a ser copiado e a pasta de destino, após isso entra em um try. catch que vai tentar copiar os arquivos e caso existam eles são substituídos
     * Caso o código entre no primeiro catch ele verifica se o erro gerado está relacionado a uso do arquivo e pausa a thread pro 1s
     * </p>
     *
     * @param sourceFile        Arquivo de origem
     * @param destinationFolder Pasta de destino
     * @throws IOException Exceção de acesso com fluxo de arquivos
     */
    public void copySingleFile(Path sourceFile, Path destinationFolder) throws IOException {
        try {
            Files.copy(sourceFile, destinationFolder, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            if (ioException instanceof FileSystemException) {
                try {
                    Thread.sleep(1000);
                    Files.copy(sourceFile, destinationFolder, StandardCopyOption.REPLACE_EXISTING);

                } catch (IOException | InterruptedException exception) {
                    Platform.runLater(() -> {
                        new DefaultAlert()
                                .alert(Alert.AlertType.ERROR, "Erro ao copiar arquivo: " + exception.getMessage(), "Erro ao copiar!");
                    });

                    throw new RuntimeException("Erro ao copiar arquivo: " + exception.getMessage(), exception);
                }
            } else {
                throw ioException;
            }
        }
    }
}
