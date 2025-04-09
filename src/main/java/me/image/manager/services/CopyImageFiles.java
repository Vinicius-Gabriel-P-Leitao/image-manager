package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CopyImageFiles {
    private Alert alert;

    /**
     * Cria e retorna uma Task do JavaFX para copiar arquivos de imagem de um diretório de origem
     * para um diretório de destino, com tratamento de diversos cenários e feedback visual.
     *
     * <p>A Task realiza as seguintes operações:</p>
     * <ul>
     *   <li>Verifica e prepara o diretório de destino (cria se não existir ou ajusta o nome se necessário)</li>
     *   <li>Filtra apenas arquivos com extensões válidas (.png, .tiff, .jpg, .jpeg, .webp)</li>
     *   <li>Trata tanto arquivos únicos quanto diretórios completos</li>
     *   <li>Fornece feedback de progresso durante a cópia</li>
     *   <li>Exibe alertas sobre arquivos inválidos ou ignorados</li>
     * </ul>
     *
     * <p><b>Observações:</b></p>
     * <ul>
     *   <li>Deve ser executada em uma thread background (não na thread da UI JavaFX)</li>
     *   <li>Atualizações da interface são feitas de forma thread-safe usando Platform.runLater</li>
     *   <li>Arquivos existentes no destino são ignorados (não sobrescritos)</li>
     * </ul>
     *
     * @param originPath      caminho do arquivo ou diretório de origem a ser copiado
     * @param destinationPath caminho do diretório de destino para onde os arquivos serão copiados
     * @return Task<Void> configurada para executar a operação de cópia
     * @throws RuntimeException se ocorrer um erro ao preparar os diretórios ou listar arquivos
     * @see Task
     * @see Files
     * @see Alert
     * @since 0.0.1
     */
    public Task<Void> createTaskCopyFiles(Path originPath, Path destinationPath) {
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
                    List<String> notValidExtension = new ArrayList<>();
                    List<String> skippedFiles = new ArrayList<>();

                    if (Files.isRegularFile(originPath)) {
                        String fileName = originPath.getFileName().toString();
                        boolean isValidFile = extensions.stream().anyMatch(fileName::endsWith);

                        if (isValidFile) {
                            Path destinationFile = finalDestinationPath.resolve(originPath.getFileName());
                            copySingleFile(originPath, destinationFile);
                        } else {
                            notValidExtension.add(fileName);
                            updateMessage("Arquivo inválido: " + fileName);
                            throw new Exception("Falha ao processar arquivo inválido");
                        }
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

                                try {
                                    copySingleFile(sourceFile, destinationFile);

                                    copiedFiles++;
                                    updateProgress(copiedFiles, totalFiles);
                                } catch (RuntimeException | IOException exception) {
                                    updateMessage(exception.getMessage());
                                    throw exception;
                                }
                            }
                        }
                    }

                    Platform.runLater(() -> {
                        if (!notValidExtension.isEmpty()) {
                            alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Os diretórios podem conter arquivos inválidos!");
                            alert.setHeaderText("Os diretórios contem arquivos que são incompatíveis com o programa!");
                            alert.setContentText("Alguns arquivos não podem ser copiados e foram ignorados: \n" + notValidExtension);
                            alert.showAndWait();
                        }

                        if (!skippedFiles.isEmpty()) {
                            alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Arquivos Ignorados!");
                            alert.setHeaderText("Alguns arquivos foram ignorados");
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
     * Copia um arquivo com substituição segura (quase atômica) e tratamento para arquivos em uso.
     *
     * <p>Este método realiza uma cópia atômica de um arquivo, com as seguintes características:</p>
     * <ul>
     *   <li>Substitui o arquivo de destino se já existir</li>
     *   <li>Se o arquivo estiver em uso, aguarda 1 segundo e tenta novamente</li>
     *   <li>Propaga exceções de I/O não relacionadas a arquivos em uso</li>
     * </ul>
     *
     * <p><b>Fluxo de operação:</b></p>
     * <ol>
     *   <li>Tenta copiar o arquivo imediatamente</li>
     *   <li>Se falhar por arquivo em uso:
     *     <ul>
     *       <li>Pausa a thread por 1 segundo</li>
     *       <li>Tenta uma nova cópia</li>
     *     </ul>
     *   </li>
     *   <li>Se a segunda tentativa falhar, lança RuntimeException</li>
     * </ol>
     *
     * @param sourceFile        Arquivo de origem a ser copiado (não pode ser null)
     * @param destinationFolder Destino da cópia (incluindo o nome do arquivo) (não pode ser null)
     * @throws IOException      Se ocorrer um erro de I/O não relacionado a arquivo em uso
     * @throws RuntimeException Se a cópia falhar após a segunda tentativa (com causa original)
     * @see java.nio.file.Files#copy
     * @see java.nio.file.StandardCopyOption#REPLACE_EXISTING
     * @since 0.0.1
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
                    throw new RuntimeException("Erro ao copiar arquivo: " + exception.getMessage(), exception);
                }
            } else {
                throw ioException;
            }
        }
    }
}
