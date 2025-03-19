package me.image.manager.services;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CopyImagesFilesTest extends ApplicationTest {
    private CopyImageFiles copyImagesFiles;

    @Override
    public void start(Stage stage) {
        copyImagesFiles = new CopyImageFiles();
    }

    /**
     * @throws IOException
     */
    @BeforeEach
    void setUp() throws IOException {
        Path directory = Paths.get("src/test/resources/destination");

        if (Files.exists(directory)) {
            try (Stream<Path> files = Files.walk(directory)) {
                files.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new RuntimeException("Erro ao excluir: " + path, e);
                    }
                });
            }

            // Garante que o próprio diretório seja deletado
            Files.deleteIfExists(directory);
        }
    }

    /**
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testCopyFilesWithValidPaths() throws IOException, ExecutionException, InterruptedException {
        Path originPath = Paths.get("src/test/resources/origin/image.png");
        Path destinationPath = Paths.get("src/test/resources/destination");
        ProgressBar progressBar = new ProgressBar();

        Path destinationFilePath = destinationPath.resolve("image.png");
        copyImagesFiles.createTaskCopyFiles(originPath, destinationFilePath, progressBar);


        assertTrue(Files.exists(destinationFilePath), "O arquivo copiado não foi encontrado.");
        assertTrue(Files.exists(destinationPath), "A pasta de destino não foi encontrada.");

        Platform.runLater(() -> progressBar.setProgress(0.5));
        progressBar.getProgress();
    }

    /**
     *
     */
    @Test
    public void testCopyFilesWithNullPaths() {
        ProgressBar progressBar = new ProgressBar();

        try {
            copyImagesFiles.createTaskCopyFiles(null, null, progressBar);
        } catch (RuntimeException exception) {
            assert exception.getMessage().contains("Os diretórios não podem ser vazios.");
        }
    }

    /**
     *
     */
    @Test
    public void testAlertOnFileCopyError() {
        Path originPath = Paths.get("invalid/path/to/");
        Path destinationPath = Paths.get("destination/path");
        ProgressBar progressBar = new ProgressBar();

        try {
            copyImagesFiles.createTaskCopyFiles(originPath, destinationPath, progressBar);
        } catch (RuntimeException exception) {
            assert exception.getMessage().contains("Erro ao listar arquivos");
        }
    }
}