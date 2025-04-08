package me.image.manager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

public class HelloController {
    // Copy UI stackpane_origin_copy
    @FXML
    private TextField text_field_origin_copy;
    @FXML
    private TextField text_field_destination_copy;
    @FXML
    public ProgressBar progressbar_copy;
    @FXML
    private ComboBox<Object> combo_box_origin_copy;
    @FXML
    private StackPane stackpane_origin_copy;
    // Rename UI
    @FXML
    private TextField text_field_origin_rename;
    @FXML
    public TextField text_field_name_file;
    @FXML
    public ProgressBar progressbar_rename;
    @FXML
    public ComboBox<Object> combo_box_rename;
    // Convert UI
    @FXML
    private TextField text_field_origin_convert;
    @FXML
    public ProgressBar progressbar_convert;
    @FXML
    public ComboBox<Object> combo_box_convert;
    // Variáveis de ui
    private Alert alert;

    private final Command<MouseEvent> mousePointer = new OnPointerMouseCommand();
    private final Command<Map.Entry<ActionEvent, OpenDirectoryChooserContext>> openDirCommand = new OnOpenDirectoryChooserCommand();
    private final Command<Map.Entry<ActionEvent, OnCopyFilesContext>> copyFilesCommand = new OnCopyFilesCommand();
    private final Command<Map.Entry<ActionEvent, OnRenameFilesContext>> renameFilesCommand = new OnRenameFilesCommand();
    private final Command<Map.Entry<ActionEvent, OnConvertFilesContext>> convertFilesCommand = new OnConvertFilesCommand();

    @FXML
    private void onPointerMouse(MouseEvent event) {
        mousePointer.execute(event);
    }

    @FXML
    private void onOpenDirectoryChooser(ActionEvent event) {
        OpenDirectoryChooserContext context = new OpenDirectoryChooserContext(combo_box_origin_copy, text_field_origin_copy, text_field_destination_copy, text_field_origin_rename, text_field_origin_convert, stackpane_origin_copy);
        openDirCommand.execute(Map.entry(event, context));
    }

    @FXML
    private void onCopyFiles(ActionEvent event) {
        OnCopyFilesContext context = new OnCopyFilesContext(combo_box_origin_copy, text_field_origin_copy, text_field_destination_copy, progressbar_copy);
        copyFilesCommand.execute(Map.entry(event, context));
    }

    // NOTE: Implementar command
    @FXML
    private void onRenameFiles(ActionEvent event) {
        OnRenameFilesContext context = new OnRenameFilesContext(text_field_origin_rename, text_field_name_file, progressbar_rename, combo_box_rename);
        renameFilesCommand.execute(Map.entry(event, context));
    }

    // NOTE: Implementar command
    @FXML
    public void onConvertFiles(ActionEvent event) {
        OnConvertFilesContext context = new OnConvertFilesContext(text_field_origin_convert, progressbar_convert, combo_box_convert);
        convertFilesCommand.execute(Map.entry(event, context));
    }
}