package me.image.manager.command.context;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class OpenDirectoryChooserContext {
    public ComboBox<Object> objectComboBox;
    public TextField textFieldOriginCopy;
    public TextField textFieldDestinationCopy;
    public TextField textFieldOriginRename;
    public TextField textFieldOriginConvert;
    public StackPane stackPaneOriginCopy;

    public OpenDirectoryChooserContext(ComboBox<Object> objectComboBox, TextField textFieldOriginCopy, TextField textFieldDestinationCopy, TextField textFieldOriginRename, TextField textFieldOriginConvert, StackPane stackPaneOriginCopy) {
        this.objectComboBox = objectComboBox;
        this.textFieldOriginCopy = textFieldOriginCopy;
        this.textFieldDestinationCopy = textFieldDestinationCopy;
        this.textFieldOriginRename = textFieldOriginRename;
        this.textFieldOriginConvert = textFieldOriginConvert;
        this.stackPaneOriginCopy = stackPaneOriginCopy;
    }
}
