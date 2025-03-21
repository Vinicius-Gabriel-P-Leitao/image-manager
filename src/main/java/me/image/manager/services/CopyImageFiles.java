package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CopyImageFiles {
    private Alert alert;

    public Task<Void> createTaskCopyFiles(Path originPath, Path destinationPath, ProgressBar progressBar) {
        try {
            if (!Files.exists(destinationPath)) {
                Files.createDirectories(destinationPath);
            } else if (!Files.isDirectory(destinationPath)) {

                Path parentDir = destinationPath.getParent();
                if (parentDir != null && !Files.exists(parentDir)) {
                    Files.createDirectories(parentDir);
                }

                destinationPath = destinationPath.resolveSibling(destinationPath.getFileName() + "_folder");
                Files.createDirectories(destinationPath);
            }

            Path finalDestinationPath = destinationPath;

            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    List<String> extensions = List.of(".png", ".tiff", ".jpg", ".jpeg", ".webp");
                    List<String> skippedFiles = new ArrayList<>();
                    List<String> notValidExtension = new ArrayList<>();

                    if (Files.isRegularFile(originPath)) {
                        Path destinationFile = finalDestinationPath.resolve(originPath.getFileName());
                        copySingleFile(originPath, destinationFile);

                    } else if (Files.isDirectory(originPath)) {

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
                                Path destinationFile = finalDestinationPath.resolve(sourceFile.getFileName());

                                if (Files.exists(destinationFile)) {
                                    skippedFiles.add(sourceFile.getFileName().toString());
                                    continue;
                                }

                                copySingleFile(sourceFile, destinationFile);

                                copiedFiles++;
                                updateProgress(copiedFiles, totalFiles);
                            }
                        }
                    }

                    Platform.runLater(() -> {
                        if (!notValidExtension.isEmpty()) {
                            alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Os diretórios podem conter arquivos inválidos!");
                            alert.setHeaderText("Os diretórios contem arquivos que são incompatíveis com o programa!");
                            alert.setContentText("Alguns arquivos não podem ser copiados: \n" + notValidExtension);
                            alert.showAndWait();
                        }

                        if (!skippedFiles.isEmpty()) {
                            alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Arquivos Ignorados");
                            alert.setHeaderText("Arquivos Ignorados");
                            alert.setContentText("Os seguintes arquivos já existem e foram ignorados: " + skippedFiles.stream().limit(5).toList() + "...");
                            alert.showAndWait();
                        }
                    });

                    return null;
                }
            };
        } catch (IOException exception) {
            Platform.runLater(() -> {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro ao listar arquivos");
                alert.setHeaderText("Erro ao listar arquivos!");
                alert.setContentText("Erro ao listar arquivos: " + exception.getMessage());
                alert.showAndWait();
            });

            throw new RuntimeException("Erro ao listar arquivos: " + exception.getMessage(), exception);
        }
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
    private void copySingleFile(Path sourceFile, Path destinationFolder) throws IOException {
        try {
            Files.copy(sourceFile, destinationFolder, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            if (ioException instanceof FileSystemException) {
                try {
                    Thread.sleep(1000);
                    Files.copy(sourceFile, destinationFolder, StandardCopyOption.REPLACE_EXISTING);

                } catch (IOException | InterruptedException exception) {
                    Platform.runLater(() -> {
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro ao copiar!");
                        alert.setHeaderText("Erro ao copiar!");
                        alert.setContentText("Erro ao copiar arquivo: " + exception.getMessage());
                        alert.showAndWait();
                    });

                    throw new RuntimeException("Erro ao copiar arquivo: " + exception.getMessage(), exception);
                }
            } else {
                ioException.printStackTrace();
                throw ioException;
            }
        }
    }
}
