package me.image.manager.command.events;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import me.image.manager.command.Command;
import me.image.manager.command.context.OpenDirectoryChooserContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

public class OnOpenDirectoryChooserCommand implements Command<Map.Entry<ActionEvent, OpenDirectoryChooserContext>> {

    /**
     * Manipula a abertura do seletor de diretórios e atualiza os componentes de UI correspondentes.
     *
     * <p>Este método implementa a interface {@code ActionHandler} e realiza as seguintes operações:</p>
     * <ol>
     *   <li>Abre um diálogo {@link DirectoryChooser} para seleção de diretório</li>
     *   <li>Atualiza diferentes componentes de UI baseado no botão que acionou o evento</li>
     *   <li>Trata especialmente o caso do botão de origem para cópia, populando um ComboBox</li>
     *   <li>Atualiza TextFields correspondentes aos botões de ação</li>
     * </ol>
     *
     * <p><b>Fluxo principal:</b></p>
     * <ul>
     *   <li>Configura e exibe o seletor de diretórios</li>
     *   <li>Se o botão for "button_origin_copy":
     *     <ul>
     *       <li>Popula um ComboBox com o diretório selecionado e seu conteúdo</li>
     *       <li>Substitui o TextField pelo ComboBox na interface</li>
     *     </ul>
     *   </li>
     *   <li>Para outros botões, atualiza o TextField correspondente com o caminho selecionado</li>
     * </ul>
     *
     * @param entry Par contendo:
     *              <ul>
     *                <li>{@link ActionEvent}: evento que disparou a execução</li>
     *                <li>{@link OpenDirectoryChooserContext}: contexto com componentes UI</li>
     *              </ul>
     * @throws RuntimeException Se nenhum diretório for selecionado no diálogo
     * @see DirectoryChooser
     * @see ComboBox
     * @since 0.0.2
     */
    @Override
    public void execute(Map.Entry<ActionEvent, OpenDirectoryChooserContext> entry) {
        ActionEvent event = entry.getKey();
        OpenDirectoryChooserContext context = entry.getValue();

        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        DirectoryChooser pathChooser = new DirectoryChooser();
        pathChooser.setInitialDirectory(Paths.get(System.getProperty("user.dir")).toFile());
        pathChooser.setTitle("Escolher pastas");

        File path = pathChooser.showDialog((Stage) stage);
        if (path == null) throw new RuntimeException("A pasta não foi selecionada!");

        if (source instanceof Button button) {
            String buttonId = button.getId();

            if ("button_origin_copy".equals(buttonId)) {
                File[] listFiles = path.listFiles();

                if (listFiles != null) {
                    ComboBox<Object> comboBox = context.objectComboBox;
                    comboBox.getItems().clear();
                    comboBox.setValue(path);
                    comboBox.getItems().add(path);

                    for (File file : listFiles) {
                        String displayName = file.isDirectory() ? "Pasta: " + file.getAbsolutePath() : "Arquivo: " + file.getAbsolutePath();
                        comboBox.getItems().add(displayName);
                    }

                    context.textFieldOriginCopy.setVisible(false);
                    comboBox.setVisible(true);

                    context.stackPaneOriginCopy.getChildren().clear();
                    context.stackPaneOriginCopy.getChildren().add(comboBox);
                }
            }
        }

        Object elementActionThisEvent = event.getSource();
        if (elementActionThisEvent instanceof Button button) {
            Map<String, TextField> buttonToTextField = Map.of(
                    "button_origin_copy", context.textFieldOriginCopy,
                    "button_destination_copy", context.textFieldDestinationCopy,
                    "button_directory_origin_rename", context.textFieldOriginRename,
                    "button_directory_origin_convert", context.textFieldOriginConvert
            );

            TextField textField = buttonToTextField.get(button.getId());
            if (textField != null) {
                textField.setText(path.toString());
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Não foi possível selecionar a pasta!");
                alert.setHeaderText("Não foi possível selecionar a pasta!");
                alert.setContentText("Erro ao selecionar pasta!");
                alert.showAndWait();
            }
        }
    }
}
