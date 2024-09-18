package com.eggplanters.le3;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class EntryController {
    @FXML
    HBox parent;

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
        try {
            openNewWindow(stage, file);
        } catch (IOException e) {
            e.printStackTrace();
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

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root, 640, 480));

        newStage.show();
    }
}
