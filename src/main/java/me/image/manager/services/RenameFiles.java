package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameFiles {
    Alert alert;

    private static String formatedDate(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, new Locale("pt", "BR"));
        return sdf.format(date);
    }

    public Task<Void> createTaskRenameFiles(Path originPath, String nameFile, String regexToName, String pattern) {
        return new Task<Void>() {
            final AtomicInteger index = new AtomicInteger(0);

            @Override
            protected Void call() throws Exception {
                Files.list(originPath).filter(Files::isRegularFile).forEach(file -> {
                    Pattern patternRegex = Pattern.compile(regexToName);
                    String fileName = file.getFileName().toString();

                    int currentIndex = index.getAndIncrement();

                    try {
                        BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                        FileTime fileTime = attributes.lastModifiedTime();
                        Date modificationDate = new Date(fileTime.toMillis());

                        String formattedDate = formatedDate(modificationDate, pattern);
                        Matcher matcher = patternRegex.matcher(formattedDate);

                        Path parentPath = file.getParent();
                        Path newFileName = Path.of(String.format("%s/%s_%s_(%d)%s", parentPath, nameFile, formattedDate, currentIndex, fileName.substring(fileName.lastIndexOf("."))));
                        if (matcher.matches()) {
                            Files.move(file, newFileName);
                        } else {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Data com formato incorreto!");
                                alert.setHeaderText("Data com formato incorreto!");
                                alert.setContentText("Não foi possível converter o seu arquivo devido a data!");
                                alert.showAndWait();
                            });

                        }
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                });
                return null;
            }
        };
    }
}