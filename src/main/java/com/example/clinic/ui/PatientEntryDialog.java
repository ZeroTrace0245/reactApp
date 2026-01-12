package com.example.clinic.ui;

import com.example.clinic.model.PatientRecord;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class PatientEntryDialog {
    private PatientEntryDialog() {
    }

    public static Optional<PatientRecord> request(Window owner, PatientRecord existing) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "New Patient" : "Edit Patient");

        Text header = new Text(dialog.getTitle());
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.setFill(Color.web("#f4f7ff"));

        Label nameLabel = new Label("Full Name");
        nameLabel.setTextFill(Color.web("#9ba4c0"));
        TextField nameField = new TextField();
        nameField.setPromptText("Patient name");

        Label statusLabel = new Label("Status");
        statusLabel.setTextFill(Color.web("#9ba4c0"));
        TextField statusField = new TextField();
        statusField.setPromptText("Current status");

        Label roomLabel = new Label("Room");
        roomLabel.setTextFill(Color.web("#9ba4c0"));
        TextField roomField = new TextField();
        roomField.setPromptText("Room number");

        if (existing != null) {
            nameField.setText(existing.getName());
            statusField.setText(existing.getStatus());
            roomField.setText(existing.getRoom());
        }

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> dialog.close());
        cancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #f5f7ff; -fx-border-color: #2d3646; -fx-border-radius: 6; -fx-pref-width: 110;");

        Button submit = new Button("Save");
        submit.setStyle("-fx-background-color: #5865f2; -fx-text-fill: white; -fx-font-weight: 600; -fx-pref-width: 110;");

        AtomicReference<PatientRecord> result = new AtomicReference<>();
        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            String status = statusField.getText().trim();
            String room = roomField.getText().trim();
            if (name.isEmpty() || status.isEmpty() || room.isEmpty()) {
                return;
            }
            result.set(new PatientRecord(name, status, room));
            dialog.close();
        });

        HBox actions = new HBox(12, cancel, submit);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));

        VBox form = new VBox(8,
                nameLabel,
                nameField,
                statusLabel,
                statusField,
                roomLabel,
                roomField
        );

        VBox root = new VBox(14, header, form, actions);
        root.setPadding(new Insets(20));
        root.setSpacing(6);
        root.setStyle("-fx-background-color: #11141f; -fx-border-radius: 20; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.65), 12, 0, 0, 4);");
        VBox.setVgrow(form, Priority.ALWAYS);

        dialog.setScene(new Scene(root, 360, 340));
        dialog.showAndWait();
        return Optional.ofNullable(result.get());
    }
}
