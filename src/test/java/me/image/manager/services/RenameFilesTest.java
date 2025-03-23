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
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RenameFilesTest extends ApplicationTest {
    private static final String DATE_HYPHEN = "^(0[0-9]|1[0-9]|2[0-9]|3[01])\\-(0[0-9]|1[0-2])\\-\\d{4}\\_(0[0-9]|1[0-9]|2[0-3])\\-(0[0-9]|[1-5][0-9])\\-(0[0-9]|[1-5][0-9])$";

    private RenameFiles renameImagesFiles;

    @Override
    public void start(Stage stage) {
        renameImagesFiles = new RenameFiles();
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

    @Test
    void testRenameFiles() throws IOException, InterruptedException {
        Path originPath = Paths.get("src/test/resources/origin/rename/");

        Task<Void> renameTask = renameImagesFiles.createTaskRenameFiles(originPath, "Renamed", DATE_HYPHEN, "dd-MM-yyyy_HH-mm-ss");
        Thread threadCopy = new Thread(renameTask);
        threadCopy.start();
        threadCopy.join();

        try (var stream = Files.list(originPath)) {
            List<Path> files = stream.toList();
            long renamedCount = files.stream()
                    .filter(path -> path.getFileName().toString().startsWith("Renamed_") && path.getFileName().toString().endsWith(".png"))
                    .count();
            assertEquals(1, renamedCount);

            assertTrue(Files.exists(originPath), "O diretório de origem não existe.");
            assertTrue(Files.isDirectory(originPath), "O caminho fornecido não é um diretório.");
        }
    }
}