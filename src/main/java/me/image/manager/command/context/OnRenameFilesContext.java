package me.image.manager.command.context;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class OnRenameFilesContext {
    public TextField textFieldOriginRename;
    public TextField textFieldNameFile;
    public ProgressBar progressbarRename;
    public ComboBox<Object> objectComboBox;

    public OnRenameFilesContext(TextField textFieldOriginRename, TextField textFieldNameFile, ProgressBar progressbarRename, ComboBox<Object> objectComboBox) {
        this.textFieldOriginRename = textFieldOriginRename;
        this.textFieldNameFile = textFieldNameFile;
        this.progressbarRename = progressbarRename;
        this.objectComboBox = objectComboBox;
    }
}
