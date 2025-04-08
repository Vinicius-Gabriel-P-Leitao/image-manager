package me.image.manager.command.events;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import me.image.manager.command.Command;

public class OnPointerMouseCommand implements Command<MouseEvent> {
    @Override
    public void execute(MouseEvent context) {
        Object elementActionThisEvent = context.getSource();
        if (elementActionThisEvent instanceof Button button) {
            button.setCursor(Cursor.HAND);
        }
    }
}
