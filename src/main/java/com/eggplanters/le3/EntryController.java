package com.eggplanters.le3;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class EntryController {
    @FXML
    HBox parent;
    @FXML
    ListView<String> fileHistoryListView;

    private List<Stage> childrenStages;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            childrenStages = new ArrayList<>();
            Stage stage = getPrimaryStage();

            stage.setOnCloseRequest(event -> {
                for (Stage childStage : childrenStages) {
                    if (childStage.isShowing()) {
                        childStage.fireEvent(new WindowEvent(childStage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    }
                }

                if (childrenStages.stream().anyMatch(Stage::isShowing)) {

                    event.consume();
                }

                FileHistory.getInstance().save();
            });

            fileHistoryListView.setItems(FileHistory.getInstance().getFileHistory());
            fileHistoryListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {

                    ObservableList<Integer> selectedIndices = fileHistoryListView.getSelectionModel()
                            .getSelectedIndices();

                    try {
                        if (event.getPickResult().getIntersectedNode().getClass() == Class
                                .forName("com.sun.javafx.scene.control.LabeledText"))
                            for (Integer o : selectedIndices) {
                                var file = new File(FileHistory.getInstance().getFileHistory().get(o));
                                openNewWindow(stage, file);
                            }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
        });
    }

    @FXML
    public void handleNew() throws IOException {
        openNewWindow(getPrimaryStage(), null);
    }

    @FXML
    public void handleLoad() {
        Stage stage = getPrimaryStage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load file...");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showOpenDialog(stage);
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
        Stage stage = getPrimaryStage();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private Stage getPrimaryStage() {
        return (Stage) parent.getScene().getWindow();
    }

    private void openNewWindow(Stage ownerStage, File file) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("texteditor.fxml"));
        Parent root = fxmlLoader.load();
        TextEditorController controller = fxmlLoader.getController();

        if (file != null) {
            controller.setFile(file);
            FileHistory.getInstance().add(file.getAbsolutePath());
        }

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root, 640, 480));
        newStage.initOwner(ownerStage);
        newStage.show();

        childrenStages.add(newStage);
    }
}
