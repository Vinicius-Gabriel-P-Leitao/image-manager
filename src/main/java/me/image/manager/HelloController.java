package me.image.manager;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import me.image.manager.components.DefaultAlert;
import me.image.manager.services.CopyImageFiles;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class HelloController {
    public TextField text_field_origin_copy;
    public TextField text_field_destination_copy;
    public TextField text_field_origin_rename;
    public TextField text_field_origin_convert;
    public ProgressBar progressBar;
    private Stage stage;

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
                new DefaultAlert().alert(Alert.AlertType.ERROR, "Não foi possível selecionar a pasta!", "Erro ao selecionar pasta!");
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
        Button button = (Button) event.getSource();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Path originPath = Path.of(text_field_origin_copy.getText().trim());
                Path destinationPath = Path.of(text_field_destination_copy.getText().trim());

                if (!Files.exists(Path.of(text_field_origin_copy.getText()))) {
                    Platform.runLater(() -> {
                        new DefaultAlert().alert(Alert.AlertType.ERROR, String.format("A origem não existe: %s", text_field_origin_copy.getText()), "Pasta de origem incorreta");
                    });

                    throw new RuntimeException(String.format("A pasta de origem não existe: %s", text_field_origin_copy.getText()));
                }

                File[] files = originPath.toFile().listFiles();
                if (files == null) {
                    throw new RuntimeException("Não foi possível acessar os arquivos na pasta de origem.");
                }

                Task<Void> copyTask = new CopyImageFiles().createTaskCopyFiles(originPath, destinationPath, progressBar);

                if (copyTask != null) {
                    Platform.runLater(() -> {
                        progressBar.progressProperty().bind(copyTask.progressProperty());
                    });

                    copyTask.setOnSucceeded(event -> {
                        Platform.runLater(() -> {
                            new DefaultAlert().alert(Alert.AlertType.INFORMATION, "Copiado com sucesso!", "Todos os arquivos foram copiados com sucesso!");
                        });
                    });
                    copyTask.setOnFailed(event -> {
                        Throwable exception = event.getSource().getException();
                        Platform.runLater(() -> {
                            new DefaultAlert().alert(Alert.AlertType.ERROR, "Erro na cópiar: " + exception.getMessage(), "Ocorreu um erro na cópia dos arquivos.");
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