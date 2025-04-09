package me.image.manager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import me.image.manager.command.Command;
import me.image.manager.command.context.OnConvertFilesContext;
import me.image.manager.command.context.OnCopyFilesContext;
import me.image.manager.command.context.OnRenameFilesContext;
import me.image.manager.command.context.OpenDirectoryChooserContext;
import me.image.manager.command.events.*;

import java.util.Map;

/**
 * Controlador principal da aplicação JavaFX.
 * Gerencia as ações da interface gráfica e coordena operações de cópia, renomeação e conversão de arquivos.
 */
public class HelloController {
    // Componentes UI - Cópia
    @FXML
    private TextField text_field_origin_copy;
    @FXML
    private TextField text_field_destination_copy;
    @FXML
    private ProgressBar progressbar_copy;
    @FXML
    private ComboBox<Object> combo_box_origin_copy;
    @FXML
    private StackPane stackpane_origin_copy;
    // Componentes UI - Renomeação
    @FXML
    private TextField text_field_origin_rename;
    @FXML
    private TextField text_field_name_file;
    @FXML
    private ProgressBar progressbar_rename;
    @FXML
    private ComboBox<Object> combo_box_rename;
    // Componentes UI - Conversão
    @FXML
    private TextField text_field_origin_convert;
    @FXML
    private ProgressBar progressbar_convert;
    @FXML
    private ComboBox<Object> combo_box_convert;

    private final Command<MouseEvent> mousePointer = new OnPointerMouseCommand();
    private final Command<Map.Entry<ActionEvent, OpenDirectoryChooserContext>> openDirCommand = new OnOpenDirectoryChooserCommand();
    private final Command<Map.Entry<ActionEvent, OnCopyFilesContext>> copyFilesCommand = new OnCopyFilesCommand();
    private final Command<Map.Entry<ActionEvent, OnRenameFilesContext>> renameFilesCommand = new OnRenameFilesCommand();
    private final Command<Map.Entry<ActionEvent, OnConvertFilesContext>> convertFilesCommand = new OnConvertFilesCommand();

    /**
     * Manipula eventos de movimento do mouse na interface.
     *
     * @param event O evento de mouse capturado
     */
    @FXML
    private void onPointerMouse(MouseEvent event) {
        mousePointer.execute(event);
    }

    /**
     * Abre o seletor de diretórios para operações.
     *
     * @param event Evento de ação que disparou o método
     */
    @FXML
    private void onOpenDirectoryChooser(ActionEvent event) {
        OpenDirectoryChooserContext context = new OpenDirectoryChooserContext(combo_box_origin_copy, text_field_origin_copy, text_field_destination_copy, text_field_origin_rename, text_field_origin_convert, stackpane_origin_copy);
        openDirCommand.execute(Map.entry(event, context));
    }


    /**
     * Executa a operação de cópia de arquivos.
     *
     * @param event Evento de ação que disparou o método
     */
    @FXML
    private void onCopyFiles(ActionEvent event) {
        OnCopyFilesContext context = new OnCopyFilesContext(combo_box_origin_copy, text_field_origin_copy, text_field_destination_copy, progressbar_copy);
        copyFilesCommand.execute(Map.entry(event, context));
    }

    /**
     * Executa a operação de renomeação de arquivos.
     *
     * @param event Evento de ação que disparou o método
     */
    @FXML
    private void onRenameFiles(ActionEvent event) {
        OnRenameFilesContext context = new OnRenameFilesContext(text_field_origin_rename, text_field_name_file, progressbar_rename, combo_box_rename);
        renameFilesCommand.execute(Map.entry(event, context));
    }

    /**
     * Executa a operação de conversão de arquivos.
     *
     * @param event Evento de ação que disparou o método
     */
    @FXML
    public void onConvertFiles(ActionEvent event) {
        OnConvertFilesContext context = new OnConvertFilesContext(text_field_origin_convert, progressbar_convert, combo_box_convert);
        convertFilesCommand.execute(Map.entry(event, context));
    }
}