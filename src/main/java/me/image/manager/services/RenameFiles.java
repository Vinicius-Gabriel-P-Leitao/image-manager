package me.image.manager.services;

import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class RenameFiles {
    public Task<Void> createTaskRenameFiles(Path originPath, String nameFile, String regexToName) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                renameSingleFile(originPath, nameFile, regexToName);
                return null;
            }
        };
    }

    private void renameSingleFile(Path originPath, String nameFile, String regexToName) {
        System.out.println(originPath + " : " + nameFile + " : " + regexToName);
    }
}
