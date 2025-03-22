package me.image.manager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import me.image.manager.services.ConvertImageFiles;
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
    // Copy UI
    public TextField text_field_origin_copy;
    public TextField text_field_destination_copy;
    public ProgressBar progressbar_copy;
    public ComboBox combo_box_origin_copy;
    public StackPane stackpane_origin_copy;
    // Rename UI
    public TextField text_field_origin_rename;
    public TextField text_field_name_file;
    public ProgressBar progressbar_rename;
    public ComboBox combo_box_rename;
    // Convert UI
    public TextField text_field_origin_convert;
    public ProgressBar progressbar_convert;
    public ComboBox combo_box_convert;
    // Variáveis de ui
    private Stage stage;
    private Alert alert;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onPointerMouse(MouseEvent event) {
        Object elementActionThisEvent = event.getSource();

        if (elementActionThisEvent instanceof Button button) {
            button.setCursor(Cursor.HAND);
        }
    }

    @FXML
    private void onOpenDirectoryChooser(ActionEvent event) {
        DirectoryChooser pathChooser = new DirectoryChooser();
        pathChooser.setInitialDirectory(Paths.get(System.getProperty("user.dir")).toFile());
        pathChooser.setTitle("Escolher pastas");

        File path = pathChooser.showDialog(stage);
        if (path == null) throw new RuntimeException("A pasta não foi selecionada!");

        if (event.getSource() instanceof Button button) {
            String buttonId = button.getId();

            if ("button_origin_copy".equals(buttonId)) {
                File[] listFiles = path.listFiles();

                if (listFiles != null) {
                    combo_box_origin_copy.getItems().clear();
                    combo_box_origin_copy.setValue(path);
                    combo_box_origin_copy.getItems().add(path);

                    for (File file : listFiles) {
                        String displayName = file.isDirectory() ? "Pasta: " + file.getAbsolutePath() : "Arquivo: " + file.getAbsolutePath();
                        combo_box_origin_copy.getItems().add(displayName);
                    }

                    text_field_origin_copy.setVisible(false);
                    combo_box_origin_copy.setVisible(true);

                    stackpane_origin_copy.getChildren().clear();
                    stackpane_origin_copy.getChildren().add(combo_box_origin_copy);
                }
            }
        }

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
    private void onCopyFiles(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String comboBoxSelectValue = combo_box_origin_copy.getSelectionModel().getSelectedItem() != null ? combo_box_origin_copy.getSelectionModel().getSelectedItem().toString() : "";

                    Path originFile;
                    Path originPath;
                    Path destinationPath = Path.of(text_field_destination_copy.getText().trim());

                    // Caso o combobox tenho um valor ele anula originPath e inicia o valor do originFile e no else faz ao contrário
                    if (comboBoxSelectValue != null && !comboBoxSelectValue.isEmpty() && combo_box_origin_copy.isVisible()) {
                        originPath = null;

                        comboBoxSelectValue = comboBoxSelectValue.replace("Pasta: ", "").replace("Arquivo: ", "");
                        originFile = Path.of(comboBoxSelectValue);
                    } else {
                        originFile = null;

                        String textFromTextField = text_field_origin_copy.getText().trim();
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
                            progressbar_copy.progressProperty().bind(copyTask.progressProperty());
                        });

                        copyTask.setOnSucceeded(workerStateEvent -> {
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

                        copyTask.setOnFailed(workerStateEvent -> {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Ocorreu um erro na cópia dos arquivos.");
                                alert.setHeaderText("Ocorreu um erro na cópia dos arquivos.");
                                alert.setContentText("Erro na cópiar: " + copyTask.getMessage());
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
                    String comboBoxSelectValue = combo_box_rename.getValue().toString();

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
                            ref.renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_BARS, comboBoxSelectValue);
                        }

                        if (comboBoxSelectValue.equals("dd_MMMM_yyyy_HH_mm_ss")) {
                            ref.renameTask = new RenameFiles().createTaskRenameFiles(originPath, nameFile, DATE_COMPLETE, comboBoxSelectValue);
                        }

                        if (ref.renameTask != null) {
                            Platform.runLater(() -> {
                                progressbar_rename.progressProperty().bind(ref.renameTask.progressProperty());
                            });

                            ref.renameTask.setOnSucceeded(workerStateEvent -> {
                                alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Formato de nome");
                                alert.setHeaderText("Arquivos renomeados com sucesso!");
                                alert.setContentText("Os arquivos do diretório foram renomeados com sucesso: " + originPath);
                                alert.showAndWait();

                                progressbar_rename.progressProperty().unbind();
                                progressbar_rename.setProgress(0);
                            });

                            ref.renameTask.setOnFailed(workerStateEvent -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Falha na renomeação dos arquivos.");
                                alert.setHeaderText("Falha na renomeação dos arquivos.");
                                alert.setContentText("Não foi possível continuar o renomeio de arquivos devido a um erro: " + ref.renameTask.getMessage());

                                progressbar_rename.progressProperty().unbind();
                                progressbar_rename.setProgress(0);
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

    public void onConvertFiles(ActionEvent event) {
        if (event.getSource() instanceof Button) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Path originPath = Path.of(text_field_origin_convert.getText().trim());
                    String comboBoxSelectValue = combo_box_convert.getValue().toString();

                    Task<Void> convertTask = new ConvertImageFiles().createTaskConvertFiles(originPath, comboBoxSelectValue);

                    if (convertTask != null) {
                        Platform.runLater(() -> {
                            progressbar_convert.progressProperty().bind(convertTask.progressProperty());
                        });

                        convertTask.setOnSucceeded(workerStateEvent -> {
                            alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Conversão de arquivos");
                            alert.setHeaderText("Conversão de arquivos com sucesso!");
                            alert.setContentText("Os arquivos do diretório foram convertidos com sucesso: " + originPath);
                            alert.showAndWait();

                            progressbar_convert.progressProperty().unbind();
                            progressbar_convert.setProgress(0);
                        });

                        convertTask.setOnFailed(workerStateEvent -> {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Falha na converter os arquivos.");
                            alert.setHeaderText("Falha na converter os arquivos.");
                            alert.setContentText("Não foi possível continuar a conversão de arquivos devido a um erro: " + convertTask.getMessage());

                            progressbar_convert.progressProperty().unbind();
                            progressbar_convert.setProgress(0);
                        });
                    }

                    Thread convertThread = new Thread(convertTask);
                    convertThread.setDaemon(true);
                    convertThread.start();

                    return null;
                }
            };

            new Thread(task).start();
        }
    }
}