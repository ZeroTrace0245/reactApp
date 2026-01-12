package com.example.clinic.ui;

import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public final class PromptDialog {
    private PromptDialog() {
    }

    public static Optional<String> request(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(null);
        return dialog.showAndWait();
    }
}
