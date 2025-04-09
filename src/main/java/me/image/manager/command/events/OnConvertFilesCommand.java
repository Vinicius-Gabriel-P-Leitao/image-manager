package me.image.manager.command.events;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import me.image.manager.command.Command;
import me.image.manager.command.context.OnConvertFilesContext;
import me.image.manager.services.ConvertImageFiles;

import java.nio.file.Path;
import java.util.Map;

public class OnConvertFilesCommand implements Command<Map.Entry<ActionEvent, OnConvertFilesContext>> {
    private Alert alert;

    /**
     * Executa a operação de conversão de arquivos de imagem para o formato especificado.
     *
     * <p>Este método implementa a interface {@code ActionHandler} e realiza as seguintes operações:</p>
     * <ol>
     *   <li>Obtém o caminho de origem e o formato de destino dos componentes da interface</li>
     *   <li>Cria e configura uma tarefa assíncrona para conversão dos arquivos</li>
     *   <li>Vincula a barra de progresso à tarefa de conversão</li>
     *   <li>Fornece feedback visual sobre o resultado da operação</li>
     * </ol>
     *
     * <p><b>Fluxo de execução:</b></p>
     * <ul>
     *   <li>Recupera o caminho de origem do campo {@code textFieldOriginConvert}</li>
     *   <li>Obtém o formato de destino selecionado no {@code objectComboBox}</li>
     *   <li>Cria a tarefa de conversão através de {@link ConvertImageFiles#createTaskConvertFiles}</li>
     *   <li>Configura os manipuladores de eventos para sucesso e falha</li>
     *   <li>Inicia a execução em uma thread separada</li>
     * </ul>
     *
     * @param entry Par contendo:
     *              <ul>
     *                <li>{@link ActionEvent}: evento que disparou a execução</li>
     *                <li>{@link OnConvertFilesContext}: contexto com os componentes da interface</li>
     *              </ul>
     * @see ConvertImageFiles#createTaskConvertFiles(Path, String)
     * @see Task
     * @since 0.0.2
     */
    @Override
    public void execute(Map.Entry<ActionEvent, OnConvertFilesContext> entry) {
        ActionEvent event = entry.getKey();
        OnConvertFilesContext context = entry.getValue();

        if (event.getSource() instanceof Button) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Path originPath = Path.of(context.textFieldOriginConvert.getText().trim());
                    String comboBoxSelectValue = context.objectComboBox.getValue().toString();

                    Task<Void> convertTask = new ConvertImageFiles().createTaskConvertFiles(originPath, comboBoxSelectValue);

                    if (convertTask != null) {
                        Platform.runLater(() -> {
                            context.progressbarConvert.progressProperty().bind(convertTask.progressProperty());
                        });

                        convertTask.setOnSucceeded(workerStateEvent -> {
                            alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Conversão de arquivos");
                            alert.setHeaderText("Conversão de arquivos com sucesso!");
                            alert.setContentText("Os arquivos do diretório foram convertidos com sucesso: " + originPath);
                            alert.showAndWait();

                            context.progressbarConvert.progressProperty().unbind();
                            context.progressbarConvert.setProgress(0);
                        });

                        convertTask.setOnFailed(workerStateEvent -> {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Falha na converter os arquivos.");
                            alert.setHeaderText("Falha na converter os arquivos.");
                            alert.setContentText("Não foi possível continuar a conversão de arquivos devido a um erro: " + convertTask.getMessage());

                            context.progressbarConvert.progressProperty().unbind();
                            context.progressbarConvert.setProgress(0);
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
