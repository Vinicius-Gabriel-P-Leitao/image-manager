package me.image.manager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import me.image.manager.services.CopyImageFiles;
import me.image.manager.services.RenameFiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class HelloController {
    private static final String DATE_HYPHEN = "^(0[0-9]|1[0-9]|2[0-9]|3[01])\\-(0[0-9]|1[0-2])\\-\\d{4}\\_(0[0-9]|1[0-9]|2[0-3])\\-(0[0-9]|[1-5][0-9])\\-(0[0-9]|[1-5][0-9])$";
    private static final String DATE_BARS = "^(0[0-9]|1[0-9]|2[0-9]|3[01])\\_(0[0-9]|1[0-2])\\_\\d{4}\\_(0[0-9]|1[0-9]|2[0-3])\\_(0[0-9]|[1-5][0-9])\\_(0[0-9]|[1-5][0-9])$";
    private static final String DATE_COMPLETE = "^([0-9]|0[0-9]|1[0-9]|2[0-9]|3[01])\\_([A-Za-zÀ-ú]+)\\_(\\d{4})\\_(0[0-9]|1[0-9]|2[0-3])\\_(0[0-9]|[1-5][0-9])\\_(0[0-9]|[1-5][0-9])$";
    // Variáveis de ui
    public TextField text_field_origin_copy;
    public TextField text_field_destination_copy;
    public TextField text_field_origin_rename;
    public TextField text_field_origin_convert;
    public TextField text_field_name_file;
    public ProgressBar progressbar_copy;
    public ProgressBar progressbar_rename;
    public ComboBox combo_box_rename;
    private Stage stage;
    private Alert alert;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onOpenDirectoryChooser(ActionEvent event) {
        DirectoryChooser pathChooser = new DirectoryChooser();
        pathChooser.setInitialDirectory(Paths.get(System.getProperty("user.dir")).toFile());
        pathChooser.setTitle("Escolher pastas");

        File path = pathChooser.showDialog(stage);
        if (path == null) throw new RuntimeException("A pasta não foi selecionada!");

        Object elementActionThisEvent = event.getSource();
        if (elementActionThisEvent instanceof Button button) {
            Map<String, TextField> buttonToTextField = Map.of("button_origin_copy", text_field_origin_copy, "button_destination_copy", text_field_destination_copy, "button_directory_origin_rename", text_field_origin_rename, "button_directory_origin_convert", text_field_origin_convert);

            TextField textField = buttonToTextField.get(button.getId());
            if (textField != null) {
                textField.setText(path.toString());
            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Não foi possível selecionar a pasta!");
                alert.setHeaderText("Não foi possível selecionar a pasta!");
                alert.setContentText("Erro ao selecionar pasta!");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onPointerMouse(MouseEvent event) {
        Object elementActionThisEvent = event.getSource();

        if (elementActionThisEvent instanceof Button button) {
            button.setCursor(Cursor.HAND);
        }
    }

    @FXML
    private void onCopyFiles(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Path originPath = Path.of(text_field_origin_copy.getText().trim());
                    Path destinationPath = Path.of(text_field_destination_copy.getText().trim());


                    if (!Files.exists(Path.of(text_field_origin_copy.getText()))) {
                        Platform.runLater(() -> {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Pasta de origem incorreta!");
                            alert.setHeaderText("Pasta de origem incorreta!");
                            alert.setContentText(String.format("A origem não existe: %s", text_field_origin_copy.getText()));
                            alert.showAndWait();
                        });

                        throw new RuntimeException(String.format("A pasta de origem não existe: %s", text_field_origin_copy.getText()));
                    }

                    File[] files = originPath.toFile().listFiles();
                    if (files == null) {
                        throw new RuntimeException("Não foi possível acessar os arquivos na pasta de origem.");
                    }

                    Task<Void> copyTask = new CopyImageFiles().createTaskCopyFiles(originPath, destinationPath, progressbar_copy);

                    if (copyTask != null) {
                        Platform.runLater(() -> {
                            progressbar_copy.progressProperty().bind(copyTask.progressProperty());
                        });

                        copyTask.setOnSucceeded(event -> {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Copiado com sucesso!");
                                alert.setHeaderText("Copiado com sucesso!");
                                alert.setContentText("Todos os arquivos foram copiados com sucesso!");
                                alert.showAndWait();

                                progressbar_copy.progressProperty().unbind();
                                progressbar_copy.setProgress(0);
                            });
                        });

                        copyTask.setOnFailed(event -> {
                            Throwable exception = event.getSource().getException();

                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Ocorreu um erro na cópia dos arquivos.");
                                alert.setHeaderText("Ocorreu um erro na cópia dos arquivos.");
                                alert.setContentText("Erro na cópiar: " + exception.getMessage());
                                alert.showAndWait();

                                progressbar_copy.progressProperty().unbind();
                                progressbar_copy.setProgress(0);
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

    @FXML
    private void onRenameFiles(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Path originPath = Path.of(text_field_origin_rename.getText().trim());
                    String nameFile = text_field_name_file.getText().trim();
                    String comboBoxResult = combo_box_rename.getValue().toString();

                    try {
                        Task<Void> renameTask = null;

                        if (Objects.equals(comboBoxResult, "formato")) {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Formado de nome");
                                alert.setHeaderText("Nenhum formato para foi escolhido!");
                                alert.setContentText("Não é possível continuar a formatação de nome sem o formato definido!");
                                alert.showAndWait();
                            });
                        }

                        if (comboBoxResult.equals("dd-MM-yyyy_HH-mm-ss")) {
                            renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_HYPHEN, comboBoxResult);
                        }

                        if (comboBoxResult.equals("dd_MM_yyyy_HH_mm_ss")) {
                            renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_BARS, comboBoxResult);
                        }

                        if (comboBoxResult.equals("dd_MMMM_yyyy_HH_mm_ss")) {
                            renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_COMPLETE, comboBoxResult);
                        }

                        if (renameTask != null) {
                            renameTask.setOnSucceeded(workerStateEvent -> {
                                alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Formado de nome");
                                alert.setHeaderText("Arquivos renomeados com sucesso!");
                                alert.setContentText("Os arquivos do diretório foram renomeados com sucesso: " + originPath);
                                alert.showAndWait();

                                progressbar_rename.progressProperty().unbind();
                                progressbar_rename.setProgress(0);
                            });

                            renameTask.setOnFailed(workerStateEvent -> {
                                Throwable exception = workerStateEvent.getSource().getException();

                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Falha na renomeação dos arquivos.");
                                alert.setHeaderText("Falha na renomeação dos arquivos.");
                                alert.setContentText("Não foi possível continuar o renomeio de arquivos devido a um erro: " + exception.getMessage());

                                progressbar_rename.progressProperty().unbind();
                                progressbar_rename.setProgress(0);
                            });

                            Thread copyThread = new Thread(renameTask);
                            copyThread.setDaemon(true);
                            copyThread.start();
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