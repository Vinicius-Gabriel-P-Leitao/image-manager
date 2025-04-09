package me.image.manager.command.events;

import com.google.api.services.drive.Drive;
import me.image.manager.command.Command;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Consumer;

import static me.image.manager.google.drive.DriveQuickstart.getDriveService;

public class OnConnectGoogleDrive implements Command<Consumer<Drive>> {
    @Override
    public void execute(Consumer<Drive> context) {
        try {
            Drive drive = getDriveService();
            context.accept(drive);
        } catch (IOException | GeneralSecurityException exception) {
            throw new RuntimeException(exception);
        }
    }
}
