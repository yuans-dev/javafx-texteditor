package com.eggplanters.le3;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class EntryController {
    @FXML
    HBox parent;
    @FXML
    ListView<String> fileHistoryListView;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Stage stage = (Stage) parent.getScene().getWindow();
            stage.setOnCloseRequest((e) -> {
                var fileHistory = FileHistory.getInstance();
                fileHistory.save();
            });
            fileHistoryListView.setItems(FileHistory.getInstance().fileHistory);
            fileHistoryListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {

                    ObservableList selectedIndices = fileHistoryListView.getSelectionModel().getSelectedIndices();

                    try {
                        if (event.getPickResult().getIntersectedNode().getClass() == Class
                                .forName("com.sun.javafx.scene.control.LabeledText"))
                            for (Object o : selectedIndices) {
                                var file = new File(FileHistory.getInstance().fileHistory.get((int) o));
                                openNewWindow(stage, file);
                            }
                    } catch (ClassNotFoundException e1) {

                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }

            });
        });
    }

    @FXML
    public void handleNew() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("texteditor.fxml"));
        Parent root = fxmlLoader.load();

        Stage newWindow = new Stage();

        newWindow.setScene(new Scene(root, 400, 300));

        newWindow.show();
    }

    @FXML
    public void handleLoad() {
        var stage = (Stage) parent.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file...");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Text", "*.txt"));
        var file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {

                openNewWindow(stage, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @FXML
    public void handleClose() {

    }

    public void openNewWindow(Stage stage, File file) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("texteditor.fxml"));
        Parent root = fxmlLoader.load();
        TextEditorController controller = fxmlLoader.getController();
        controller.setFile(file);
        var fileHistory = FileHistory.getInstance();
        fileHistory.fileHistory.add(file.getAbsolutePath());

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root, 640, 480));

        newStage.show();
    }
}
