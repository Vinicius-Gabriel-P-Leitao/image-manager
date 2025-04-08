package me.image.manager.command.events;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import me.image.manager.command.Command;
import me.image.manager.command.context.OnCopyFilesContext;
import me.image.manager.services.CopyImageFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class OnCopyFilesCommand implements Command<Map.Entry<ActionEvent, OnCopyFilesContext>> {
    private Alert alert;

    @Override
    public void execute(Map.Entry<ActionEvent, OnCopyFilesContext> entry) {
        ActionEvent event = entry.getKey();
        OnCopyFilesContext context = entry.getValue();

        if (event.getSource() instanceof Button) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String comboBoxSelectValue = context.objectComboBox.getSelectionModel().getSelectedItem() != null ? context.objectComboBox.getSelectionModel().getSelectedItem().toString() : "";

                    Path originFile;
                    Path originPath;
                    Path destinationPath = Path.of(context.textFieldDestinationCopy.getText().trim());

                    // Caso o combobox tenho um valor ele anula originPath e inicia o valor do originFile e no else faz ao contrário
                    if (comboBoxSelectValue != null && !comboBoxSelectValue.isEmpty() && context.objectComboBox.isVisible()) {
                        originPath = null;

                        comboBoxSelectValue = comboBoxSelectValue.replace("Pasta: ", "").replace("Arquivo: ", "");
                        originFile = Path.of(comboBoxSelectValue);
                    } else {
                        originFile = null;

                        String textFromTextField = context.textFieldOriginCopy.getText().trim();
                        if (!textFromTextField.isEmpty()) {
                            originPath = Path.of(textFromTextField);
                        } else {
                            originPath = null;
                        }
                    }

                    if ((originPath == null && originFile == null) || destinationPath.toString().isEmpty()) {
                        Platform.runLater(() -> {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro ao listar arquivos/pastas");
                            alert.setHeaderText("Erro ao listar arquivos/pastas!");

                            String origemMensagem = (originPath != null || originFile != null) ? (originPath != null ? originPath.toString() : originFile.toString()) : "Nenhum caminho de origem fornecido";
                            String destinoMensagem = Files.exists(destinationPath) ? destinationPath.toString() : "Caminho de destino inválido";

                            alert.setContentText(String.format("Os caminhos de diretório ou arquivo são inválidos: \nOrigem: %s \nDestino: %s", origemMensagem, destinoMensagem));
                            alert.showAndWait();
                        });

                        throw new RuntimeException("Os caminhos de diretório ou arquivo estão nulos ou o destino não existe");
                    }

                    Task<Void> copyTask;

                    copyTask = new CopyImageFiles().createTaskCopyFiles(Objects.requireNonNullElse(originFile, originPath), destinationPath);

                    if (copyTask != null) {
                        Platform.runLater(() -> {
                            context.progressbarCopy.progressProperty().bind(copyTask.progressProperty());
                        });

                        copyTask.setOnSucceeded(workerStateEvent -> {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Copiado com sucesso!");
                                alert.setHeaderText("Copiado com sucesso!");
                                alert.setContentText("Todos os arquivos foram copiados com sucesso!");
                                alert.showAndWait();

                                context.progressbarCopy.progressProperty().unbind();
                                context.progressbarCopy.setProgress(0);
                            });
                        });

                        copyTask.setOnFailed(workerStateEvent -> {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Ocorreu um erro na cópia dos arquivos.");
                                alert.setHeaderText("Ocorreu um erro na cópia dos arquivos.");
                                alert.setContentText("Erro na cópiar: " + copyTask.getMessage());
                                alert.showAndWait();

                                context.progressbarCopy.progressProperty().unbind();
                                context.progressbarCopy.setProgress(0);
                            });
                        });
                    }

                    Thread copyThread = new Thread(copyTask);
                    copyThread.setDaemon(true);
                    copyThread.start();

                    return null;
                }
            };

            new Thread(task).start();
        }
    }
}
