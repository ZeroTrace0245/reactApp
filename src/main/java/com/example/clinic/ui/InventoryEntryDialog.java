package com.example.clinic.ui;

import com.example.clinic.model.InventoryItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class InventoryEntryDialog {
    private InventoryEntryDialog() {
    }

    public static Optional<InventoryItem> request(Window owner, InventoryItem existing, Supplier<String> idSupplier) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Inventory Item" : "Edit Inventory Item");

        Text header = new Text(dialog.getTitle());
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.getStyleClass().add("section-heading");

        Label idLabel = new Label("Item ID");
        idLabel.getStyleClass().add("muted-label");
        TextField idField = new TextField(existing == null ? idSupplier.get() : existing.getId());
        idField.setEditable(false);
        idField.getStyleClass().add("input-field");

        Label nameLabel = new Label("Name");
        nameLabel.getStyleClass().add("muted-label");
        TextField nameField = new TextField();
        nameField.setPromptText("Item name");
        nameField.getStyleClass().add("input-field");

        Label quantityLabel = new Label("Quantity");
        quantityLabel.getStyleClass().add("muted-label");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Units on hand");
        quantityField.getStyleClass().add("input-field");

        Label priceLabel = new Label("Unit Price");
        priceLabel.getStyleClass().add("muted-label");
        TextField priceField = new TextField();
        priceField.setPromptText("0.00");
        priceField.getStyleClass().add("input-field");

        Label statusLabel = new Label("Status");
        statusLabel.getStyleClass().add("muted-label");
        TextField statusField = new TextField();
        statusField.setPromptText("Healthy / Critical / Reorder");
        statusField.getStyleClass().add("input-field");

        if (existing != null) {
            nameField.setText(existing.getName());
            quantityField.setText(String.valueOf(existing.getQuantity()));
            priceField.setText(String.format("%.2f", existing.getPrice()));
            statusField.setText(existing.getStatus());
        }

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> dialog.close());
        cancel.getStyleClass().add("ghost-button");
        cancel.setPrefWidth(110);

        Button submit = new Button("Save");
        submit.getStyleClass().add("primary-button");
        submit.setPrefWidth(110);

        AtomicReference<InventoryItem> result = new AtomicReference<>();
        submit.setOnAction(e -> {
            String name = nameField.getText().trim();
            String quantityInput = quantityField.getText().trim();
            String priceInput = priceField.getText().trim();
            String status = statusField.getText().trim();
            if (name.isEmpty() || quantityInput.isEmpty() || priceInput.isEmpty() || status.isEmpty()) {
                return;
            }
            try {
                int quantity = Integer.parseInt(quantityInput);
                double price = Double.parseDouble(priceInput);
                result.set(new InventoryItem(idField.getText(), name, quantity, price, status));
                dialog.close();
            } catch (NumberFormatException ex) {
                // keep dialog open on parse errors
            }
        });

        HBox actions = new HBox(12, cancel, submit);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));

        VBox form = new VBox(8,
                idLabel,
                idField,
                nameLabel,
                nameField,
                quantityLabel,
                quantityField,
                priceLabel,
                priceField,
                statusLabel,
                statusField
        );

        VBox root = new VBox(14, header, form, actions);
        root.setPadding(new Insets(20));
        root.setSpacing(6);
        root.getStyleClass().add("dialog-root");
        VBox.setVgrow(form, Priority.ALWAYS);

        Scene scene = new Scene(root, 420, 440);
        applyStyles(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
        return Optional.ofNullable(result.get());
    }

    private static void applyStyles(Scene scene) {
        var css = InventoryEntryDialog.class.getResource("/styles/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
