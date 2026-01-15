package com.example.clinic.ui;

import com.example.clinic.model.Appointment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class AppointmentEntryDialog {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AppointmentEntryDialog() {
    }

    public static Optional<Appointment> request(Window owner, Appointment existing, Supplier<String> idSupplier, String defaultClinician) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "New Appointment" : "Edit Appointment");

        Text header = new Text(dialog.getTitle());
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.getStyleClass().add("section-heading");

        Label idLabel = new Label("Appointment ID");
        idLabel.getStyleClass().add("muted-label");
        TextField idField = new TextField(existing == null ? idSupplier.get() : existing.getId());
        idField.setEditable(false);
        idField.getStyleClass().add("input-field");

        Label patientLabel = new Label("Patient");
        patientLabel.getStyleClass().add("muted-label");
        TextField patientField = new TextField();
        patientField.setPromptText("Patient name");
        patientField.getStyleClass().add("input-field");

        Label clinicianLabel = new Label("Clinician");
        clinicianLabel.getStyleClass().add("muted-label");
        TextField clinicianField = new TextField(defaultClinician);
        clinicianField.setPromptText("Doctor or Nurse name");
        clinicianField.getStyleClass().add("input-field");

        Label timeLabel = new Label("Schedule (yyyy-MM-dd HH:mm)");
        timeLabel.getStyleClass().add("muted-label");
        TextField timeField = new TextField();
        timeField.setPromptText("2024-06-30 14:30");
        timeField.getStyleClass().add("input-field");

        Label notesLabel = new Label("Notes");
        notesLabel.getStyleClass().add("muted-label");
        TextArea notesField = new TextArea();
        notesField.setPromptText("Reason or extra details");
        notesField.setWrapText(true);
        notesField.getStyleClass().add("input-field");

        if (existing != null) {
            patientField.setText(existing.getPatientName());
            clinicianField.setText(existing.getClinician());
            timeField.setText(existing.getScheduledAt().format(FORMATTER));
            notesField.setText(existing.getNotes());
        }

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> dialog.close());
        cancel.getStyleClass().add("ghost-button");
        cancel.setPrefWidth(110);

        Button submit = new Button("Save");
        submit.getStyleClass().add("primary-button");
        submit.setPrefWidth(110);

        AtomicReference<Appointment> result = new AtomicReference<>();
        submit.setOnAction(e -> {
            String patient = patientField.getText().trim();
            String clinician = clinicianField.getText().trim();
            String timeInput = timeField.getText().trim();
            String notes = notesField.getText().trim();
            if (patient.isEmpty() || clinician.isEmpty() || timeInput.isEmpty()) {
                return;
            }
            try {
                LocalDateTime scheduled = LocalDateTime.parse(timeInput, FORMATTER);
                result.set(new Appointment(idField.getText(), patient, clinician, scheduled, notes));
                dialog.close();
            } catch (Exception ex) {
                // keep dialog open on parse errors
            }
        });

        HBox actions = new HBox(12, cancel, submit);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));

        VBox form = new VBox(8,
                idLabel,
                idField,
                patientLabel,
                patientField,
                clinicianLabel,
                clinicianField,
                timeLabel,
                timeField,
                notesLabel,
                notesField
        );

        VBox root = new VBox(14, header, form, actions);
        root.setPadding(new Insets(20));
        root.setSpacing(6);
        root.getStyleClass().add("dialog-root");
        VBox.setVgrow(form, Priority.ALWAYS);

        Scene scene = new Scene(root, 440, 520);
        applyStyles(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
        return Optional.ofNullable(result.get());
    }

    private static void applyStyles(Scene scene) {
        var css = AppointmentEntryDialog.class.getResource("/styles/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
