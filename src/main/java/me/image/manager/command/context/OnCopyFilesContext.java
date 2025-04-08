package me.image.manager.command.context;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class OnCopyFilesContext {
    public ComboBox<Object> objectComboBox;
    public TextField textFieldOriginCopy;
    public TextField textFieldDestinationCopy;
    public ProgressBar progressbarCopy;

    public OnCopyFilesContext(ComboBox<Object> objectComboBox, TextField textFieldOriginCopy, TextField textFieldDestinationCopy, ProgressBar progressbarCopy) {
        this.objectComboBox = objectComboBox;
        this.textFieldOriginCopy = textFieldOriginCopy;
        this.textFieldDestinationCopy = textFieldDestinationCopy;
        this.progressbarCopy = progressbarCopy;
    }
}
