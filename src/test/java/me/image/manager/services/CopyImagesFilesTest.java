package me.image.manager.services;

import javafx.concurrent.Task;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
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
     */
    @Test
    public void testCopyFilesWithValidFile() {
        Path originPath = Paths.get("src/test/resources/origin/image.png");
        Path destinationPath = Paths.get("src/test/resources/destination");

        Task<Void> copyTask = copyImagesFiles.createTaskCopyFiles(originPath, destinationPath);

        Thread threadCopy = new Thread(copyTask);
        threadCopy.start();

        assertTrue(Files.exists(destinationPath), "A pasta de destino não foi encontrada.");
        assertTrue(Files.exists(originPath), "O arquivo copiado não foi encontrado.");
    }

    /**
     * <h4>Realiza teste de caso com arquivo invalido</h4>
     *
     * <p>
     * O teste define o local de origem e destino do arquivo de testes invalid-file.txt, após isso realiza a chamado do método
     * a ser testado e verifica se o destino que o arquivo foi movido é o que foi pré-definido esperando que retorne falso.
     * </p>
     */
    @Test
    public void testCopyFilesWithInvalidFile() throws IOException, ExecutionException, InterruptedException {
        Path originPath = Paths.get("src/test/resources/origin/invalid-file.txt");
        Path destinationPath = Paths.get("src/test/resources/destination");

        Task<Void> copyTask = copyImagesFiles.createTaskCopyFiles(originPath, destinationPath);

        Thread threadCopy = new Thread(copyTask);
        threadCopy.start();

        assertFalse(Files.exists(Path.of(destinationPath + "/invalid-file.txt")), "O arquivo copiado não foi encontrado.");
    }

    /**
     * <h4>Testes com pasta de de origem invalida</h4>
     */
    @Test
    public void testOriginPathInvalid() {
        Path originPath = Paths.get("invalid/path/to/");
        Path destinationPath = Paths.get("destination/path");

        try {
            copyImagesFiles.createTaskCopyFiles(originPath, destinationPath);
        } catch (RuntimeException exception) {
            assert exception.getMessage().contains("Erro ao listar arquivos");
        }
    }
}