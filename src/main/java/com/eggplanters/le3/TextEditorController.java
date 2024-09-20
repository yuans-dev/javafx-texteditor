package com.eggplanters.le3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TextEditorController {

    @FXML
    TextArea textArea;

    private boolean isTextChanged = false;
    private String documentTitle = "Untitled Document.txt";
    private File currentFile = null;
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            isTextChanged = true;
            Stage stage = (Stage) textArea.getScene().getWindow();
            stage.setTitle(documentTitle + " (Unsaved)");
        });

        Platform.runLater(() -> {
            startPeriodicBackup();
            Stage stage = (Stage) textArea.getScene().getWindow();
            stage.setTitle(documentTitle);
            stage.setOnCloseRequest(event -> {
                if (isTextChanged) {
                    event.consume();
                    promptSaveOnClose(stage);
                }
                System.out.println(currentFile.lastModified() + " - " + System.currentTimeMillis());
                scheduler.shutdown();
            });
            if (currentFile != null) {
                if (!checkForBackup()) {
                    openFile(currentFile);
                }
            }

        });
    }

    private void startPeriodicBackup() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            createBackup();
            System.out.println("Backup created at " + System.currentTimeMillis());
        }, 30, 30, TimeUnit.SECONDS); // Save every 30 seconds
    }

    private void promptSaveOnClose(Stage stage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Unsaved changes in " + documentTitle);
        dialog.setHeaderText("You have unsaved changes.");

        ButtonType saveButton = new ButtonType("Save", ButtonData.YES);
        ButtonType dontSaveButton = new ButtonType("Don't Save", ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButton, dontSaveButton, cancelButton);

        VBox vbox = new VBox();
        vbox.getChildren().add(new Label("Do you want to save your changes?"));
        dialog.getDialogPane().setContent(vbox);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == saveButton) {

                handleSave();

                if (!isTextChanged) {
                    stage.close();
                }
            } else if (result.get() == dontSaveButton) {
                isTextChanged = false;

                stage.close();
            }

        }
    }

    @FXML
    public void handleSave() {
        save(false);
    }

    @FXML
    public void handleSaveAs() {
        save(true);
    }

    public void createBackup() {
        var text = textArea.getText();

        File backupDir = new File("backups/");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        var file = new File("backups/" + "backup-" + currentFile.getName());
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(text);
            } catch (IOException e) {
                System.out.println("Something went wrong.");
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(boolean saveAsNewFile) {
        var text = textArea.getText();
        var stage = (Stage) textArea.getScene().getWindow();
        if (saveAsNewFile) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("Untitled Document");
            fileChooser.setTitle("Save your file");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Text", "*.txt"));
            var file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                documentTitle = file.getName();
                stage.setTitle(documentTitle);

                try (FileWriter fileWriter = new FileWriter(file)) {
                    fileWriter.write(text);
                    isTextChanged = false;
                    currentFile = file;
                } catch (IOException e) {
                    System.out.println("Something went wrong.");
                    e.printStackTrace();
                }

            }
        } else if (currentFile == null) {
            save(true);
            return;
        } else {

            if (currentFile != null) {
                documentTitle = currentFile.getName();
                stage.setTitle(documentTitle);

                try (FileWriter fileWriter = new FileWriter(currentFile)) {
                    fileWriter.write(text);
                    isTextChanged = false;

                } catch (IOException e) {
                    System.out.println("Something went wrong.");
                    e.printStackTrace();
                }
            }
        }

        var fileHistory = FileHistory.getInstance();
        fileHistory.add(currentFile.getAbsolutePath());
    }

    public void setFile(File file) {
        currentFile = file;
    }

    public void openFile(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = br.readLine()) != null) {
                text.append(line + "\n");
            }
            textArea.setText(text.toString());
            save(false);
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getStage() {
        var stage = (Stage) textArea.getScene().getWindow();
        return stage;
    }

    public boolean checkForBackup() {
        File backupFile = new File("backups/backup-" + currentFile.getName());

        System.out.println(backupFile.lastModified() + " - " + currentFile.lastModified());
        if (backupFile.exists() && backupFile.lastModified() > currentFile.lastModified()) {

            if (openRestoreDialog()) {
                openFile(backupFile);
                backupFile.delete();
                System.out.println("Backup restored");
                return true;
            }
        }
        return false;
    }

    public boolean openRestoreDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Backup found!");
        dialog.setHeaderText("You have a backup for " + documentTitle);

        ButtonType restoreButton = new ButtonType("Restore", ButtonData.YES);
        ButtonType dontRestoreButton = new ButtonType("Don't restore", ButtonData.NO);

        dialog.getDialogPane().getButtonTypes().addAll(restoreButton, dontRestoreButton);

        VBox vbox = new VBox();
        vbox.getChildren().add(new Label("Do you want to restore from backup?"));
        dialog.getDialogPane().setContent(vbox);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == restoreButton) {

                return true;

            } else if (result.get() == dontRestoreButton) {
                return false;
            }
        }
        return false;
    }
}
