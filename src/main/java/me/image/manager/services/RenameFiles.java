package me.image.manager.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
            @Override
            protected Void call() throws IOException {
                List<String> extensions = List.of(".png", ".tiff", ".jpg", ".jpeg", ".webp");
                List<String> notValidExtension = new ArrayList<>();

                Pattern patternRegex = Pattern.compile(regexToName);

                try (var stream = Files.list(originPath)) {
                    List<Path> filesToRename = stream.filter(sourceFile -> {
                        String fileName = sourceFile.getFileName().toString();
                        boolean isValidFile = extensions.stream().anyMatch(fileName::endsWith);

                        if (!isValidFile) {
                            notValidExtension.add(fileName);
                        }

                        return isValidFile;
                    }).toList();

                    int totalFiles = filesToRename.size();
                    int renamedFilesToProgress = 0;
                    int renamedFiles= 0;

                    for (Path sourceFile : filesToRename) {
                        BasicFileAttributes attributes = Files.readAttributes(sourceFile, BasicFileAttributes.class);
                        FileTime fileTime = attributes.lastModifiedTime();
                        Date modificationDate = new Date(fileTime.toMillis());

                        String formattedDate = formatedDate(modificationDate, pattern);
                        Matcher matcher = patternRegex.matcher(formattedDate);

                        Path parentPath = sourceFile.getParent();
                        Path newFileName = Path.of(String.format("%s/%s_%s_(%d)%s", parentPath, nameFile, formattedDate, renamedFiles++, sourceFile.toString().substring(sourceFile.toString().lastIndexOf("."))));

                        if (!newFileName.equals(sourceFile)) {
                            renamedFilesToProgress++;
                            updateProgress(renamedFilesToProgress, totalFiles);
                        }

                        if (matcher.matches()) {
                            Files.move(sourceFile, newFileName);
                        } else {
                            Platform.runLater(() -> {
                                alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Data com formato incorreto!");
                                alert.setHeaderText("Data com formato incorreto!");
                                alert.setContentText("Não foi possível converter o seu arquivo devido a data!");
                                alert.showAndWait();
                            });

                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        };
    }
}