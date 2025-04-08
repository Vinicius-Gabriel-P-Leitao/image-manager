package me.image.manager.command.events;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import me.image.manager.command.Command;
import me.image.manager.command.context.OnRenameFilesContext;
import me.image.manager.services.RenameFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class OnRenameFilesCommand implements Command<Map.Entry<ActionEvent, OnRenameFilesContext>> {
    private static final String DATE_HYPHEN = "^(0[0-9]|1[0-9]|2[0-9]|3[01])\\-(0[0-9]|1[0-2])\\-\\d{4}\\_(0[0-9]|1[0-9]|2[0-3])\\-(0[0-9]|[1-5][0-9])\\-(0[0-9]|[1-5][0-9])$";
    private static final String DATE_UNDERLINE = "^(0[0-9]|1[0-9]|2[0-9]|3[01])\\_(0[0-9]|1[0-2])\\_\\d{4}\\_(0[0-9]|1[0-9]|2[0-3])\\_(0[0-9]|[1-5][0-9])\\_(0[0-9]|[1-5][0-9])$";
    private static final String DATE_COMPLETE = "^([0-9]|0[0-9]|1[0-9]|2[0-9]|3[01])\\_([A-Za-zÀ-ú]+)\\_(\\d{4})\\_(0[0-9]|1[0-9]|2[0-3])\\_(0[0-9]|[1-5][0-9])\\_(0[0-9]|[1-5][0-9])$";

    private Alert alert;

    @Override
    public void execute(Map.Entry<ActionEvent, OnRenameFilesContext> entry) {
        ActionEvent event = entry.getKey();
        OnRenameFilesContext context = entry.getValue();

        if (event.getSource() instanceof Button) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Path originPath = Path.of(context.textFieldOriginRename.getText().trim());
                    String nameFile = context.textFieldNameFile.getText().trim();
                    String comboBoxSelectValue = context.objectComboBox.getValue().toString();

                    if (!Files.exists(originPath) || nameFile.isEmpty()) {
                        Platform.runLater(() -> {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro ao receber dados");
                            alert.setHeaderText("Erro ao receber dados!");
                            alert.setContentText(String.format("Os caminhos de diretório ou nome do arquivo são invalido:  \nOrigem: %s \nNome: %s", originPath, nameFile));
                            alert.showAndWait();
                        });

                        throw new RuntimeException(String.format("Os caminhos de diretório ou nome do arquivo são invalido:  \nOrigem: %s \nNome: %s", originPath, nameFile));
                    }

                    try {
                        var ref = new Object() {
                            Task<Void> renameTask = null;
                        };

                        if (Objects.equals(comboBoxSelectValue, "formato")) {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Formato de nome");
                                alert.setHeaderText("Nenhum formato para foi escolhido!");
                                alert.setContentText("Não é possível continuar a formatação de nome sem o formato definido!");
                                alert.showAndWait();
                            });
                        }

                        if (comboBoxSelectValue.equals("dd-MM-yyyy_HH-mm-ss")) {
                            ref.renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_HYPHEN, comboBoxSelectValue);
                        }

                        if (comboBoxSelectValue.equals("dd_MM_yyyy_HH_mm_ss")) {
                            ref.renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_UNDERLINE, comboBoxSelectValue);
                        }

                        if (comboBoxSelectValue.equals("dd_MMMM_yyyy_HH_mm_ss")) {
                            ref.renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_COMPLETE, comboBoxSelectValue);
                        }

                        if (ref.renameTask != null) {
                            Platform.runLater(() -> {
                                context.progressbarRename.progressProperty().bind(ref.renameTask.progressProperty());
                            });

                            ref.renameTask.setOnSucceeded(workerStateEvent -> {
                                alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Formato de nome");
                                alert.setHeaderText("Arquivos renomeados com sucesso!");
                                alert.setContentText("Os arquivos do diretório foram renomeados com sucesso: " + originPath);
                                alert.showAndWait();

                                context.progressbarRename.progressProperty().unbind();
                                context.progressbarRename.setProgress(0);
                            });

                            ref.renameTask.setOnFailed(workerStateEvent -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Falha na renomeação dos arquivos.");
                                alert.setHeaderText("Falha na renomeação dos arquivos.");
                                alert.setContentText("Não foi possível continuar o renomeio de arquivos devido a um erro: " + ref.renameTask.getMessage());

                                context.progressbarRename.progressProperty().unbind();
                                context.progressbarRename.setProgress(0);
                            });

                            Thread renameThread = new Thread(ref.renameTask);
                            renameThread.setDaemon(true);
                            renameThread.start();
                        }
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }

                    return null;
                }
            };

            new Thread(task).start();
        }
    }
}
