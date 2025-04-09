package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ConvertImageFiles {
    private Alert alert;

    /**
     * Cria uma tarefa assíncrona para converter arquivos de imagem para um formato especificado.
     *
     * <p>Esta Task realiza as seguintes operações:</p>
     * <ul>
     *   <li>Converte arquivos de imagem entre formatos suportados (PNG, TIFF, JPG, JPEG, WEBP)</li>
     *   <li>Move os arquivos originais para uma subpasta "old_files" após conversão bem-sucedida</li>
     *   <li>Fornece feedback de progresso durante a conversão</li>
     *   <li>Identifica e reporta arquivos que não puderam ser convertidos</li>
     * </ul>
     *
     * @param originPath Caminho do diretório contendo os arquivos a serem convertidos (não pode ser null)
     * @param fileType   Extensão do formato de destino (ex: ".png", ".jpg") - deve incluir o ponto
     * @return Task<Void> configurada para executar a operação de conversão em background
     * @throws Exception Se ocorrer um erro inesperado durante o processamento
     * @see javax.imageio.ImageIO
     * @see java.nio.file.Files
     * @see javafx.concurrent.Task
     * @since 0.0.1
     */
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

                            Path oldFilesDir = Paths.get(originPath.toString(), "old_files");
                            Files.createDirectories(oldFilesDir);

                            Path oldFilePath = Paths.get(oldFilesDir.toString(), fileName);
                            Files.move(file, oldFilePath, StandardCopyOption.REPLACE_EXISTING);
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
