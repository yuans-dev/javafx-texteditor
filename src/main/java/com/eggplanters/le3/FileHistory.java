package com.eggplanters.le3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileHistory {
    private FileHistory() {
        fileHistory = FXCollections.observableArrayList();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                fileHistory.add(line);
            }
            br.close();
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
    }

    private String filename = "file_history.txt";
    private static FileHistory instance;
    private ObservableList<String> fileHistory;

    public static FileHistory getInstance() {
        if (instance == null) {
            instance = new FileHistory();
        }
        return instance;
    }

    public ObservableList<String> getFileHistory() {
        return fileHistory;
    }

    public void add(String filename) {
        while (fileHistory.contains(filename)) {
            fileHistory.remove(filename);

        }
        fileHistory.add(filename);
    }

    public void save() {
        try {
            FileWriter fileWriter = new FileWriter(filename);

            StringBuilder historyString = new StringBuilder();
            for (String string : fileHistory) {
                historyString.append(string + "\n");
            }
            fileWriter.write(historyString.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
