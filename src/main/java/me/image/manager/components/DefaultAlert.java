package me.image.manager.components;

import javafx.scene.control.Alert;

public class DefaultAlert {
    public void alert(Alert.AlertType type, String message, String title) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
