package me.image.manager.command.context;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class OnConvertFilesContext {
    public TextField textFieldOriginConvert;
    public ProgressBar progressbarConvert;
    public ComboBox<Object> objectComboBox;

    public OnConvertFilesContext(TextField textFieldOriginConvert, ProgressBar progressbarConvert, ComboBox<Object> objectComboBox) {
        this.textFieldOriginConvert = textFieldOriginConvert;
        this.progressbarConvert = progressbarConvert;
        this.objectComboBox = objectComboBox;
    }
}
