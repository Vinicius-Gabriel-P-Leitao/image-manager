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

import static org.junit.jupiter.api.Assertions.assertTrue;

class CopyImagesFilesTest extends ApplicationTest {
    private CopyImageFiles copyImagesFiles;

    @Override
    public void start(Stage stage) {
        copyImagesFiles = new CopyImageFiles();
    }

    /**
     * <h4>Apaga a pasta de destino após executar os testes</h4>
     *
     * @throws IOException Exceção de acesso com fluxo de arquivos
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
     * <h4>Realiza teste de caso perfeito e vaida se o arquivo foi realmente movido</h4>
     *
     * <p>
     * O teste define o local de origem e destino do arquivo de testes image.png, após isso realiza a chamado do método
     * a ser testado e verifica se o destino que o arquivo foi movido é o que foi pré-definido.
     * </p>
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testCopyFilesWithValidPaths() throws IOException, ExecutionException, InterruptedException {
        Path originPath = Paths.get("src/test/resources/origin/");
        Path destinationPath = Paths.get("src/test/resources/destination");
        ProgressBar progressBar = new ProgressBar();

        Path destinationFilePath = destinationPath.resolve("image.png");
        copyImagesFiles.createTaskCopyFiles(originPath, destinationFilePath);


        assertTrue(Files.exists(destinationFilePath), "O arquivo copiado não foi encontrado.");
        assertTrue(Files.exists(destinationPath), "A pasta de destino não foi encontrada.");

        Platform.runLater(() -> progressBar.setProgress(0.5));
        progressBar.getProgress();
    }

    /**
     * <h4>Testes com pasta de de origem invalida</h4>
     */
    @Test
    public void testOriginPathInvalid() {
        Path originPath = Paths.get("invalid/path/to/");
        Path destinationPath = Paths.get("destination/path");
        ProgressBar progressBar = new ProgressBar();

        try {
            copyImagesFiles.createTaskCopyFiles(originPath, destinationPath);
        } catch (RuntimeException exception) {
            assert exception.getMessage().contains("Erro ao listar arquivos");
        }
    }
}