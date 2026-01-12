package com.example.clinic.ui;

import com.example.clinic.model.AppUser;
import com.example.clinic.service.AuthService;
import com.example.clinic.settings.SettingsStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class LoginView {
    private static final Map<String, String> FALLBACK_CREDENTIALS = Map.of(
            "ADMIN", "Admin1234",
            "DOCTOR", "Doctor1234",
            "NURSE", "Nurse1234"
    );

    private final AuthService authService;
    private final SettingsStore settingsStore;
    private final BiConsumer<Stage, AppUser> onSuccess;

    public LoginView(AuthService authService, SettingsStore settingsStore, BiConsumer<Stage, AppUser> onSuccess) {
        this.authService = authService;
        this.settingsStore = settingsStore;
        this.onSuccess = onSuccess;
    }

    public void start(Stage stage) {
        stage.setTitle("Clinic Manager Login");
        stage.setScene(new Scene(createContent(stage), 480, 360));
        stage.show();
    }

    private Parent createContent(Stage stage) {
        HBox root = new HBox(18);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: radial-gradient(center 30% 20%, radius 80%, #5b69f5, #151618 60%);");

        VBox serverBar = new VBox(14);
        serverBar.setAlignment(Pos.TOP_CENTER);
        serverBar.setPadding(new Insets(16));
        serverBar.setPrefWidth(90);
        serverBar.setStyle("-fx-background-color: #2d3138; -fx-background-radius: 18;");
        for (String icon : new String[]{"C", "P", "S", "I"}) {
            Label circle = new Label(icon);
            circle.setStyle("-fx-background-color: #5865f2; -fx-text-fill: white; -fx-font-weight: 700; -fx-padding: 6 0; -fx-alignment: center; -fx-background-radius: 16; -fx-pref-width: 48;");
            serverBar.getChildren().add(circle);
        }

        RoundedPane card = new RoundedPane(26, Color.web("#23252a"));
        card.setMaxWidth(420);

        VBox cardContent = new VBox(16);
        cardContent.setAlignment(Pos.CENTER_LEFT);
        Text heading = new Text("Clinic Manager");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        heading.setFill(Color.WHITE);
        Text subText = new Text("Custom clinic control hub");
        subText.setFill(Color.web("#9ba4c0"));
        subText.setFont(Font.font(14));

        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label("Username");
        userLabel.setTextFill(Color.web("#b9c2d5"));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setText(settingsStore.getLastUsername().orElse(""));

        Label passwordLabel = new Label("Password");
        passwordLabel.setTextFill(Color.web("#b9c2d5"));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Label statusLabel = new Label();
        statusLabel.setTextFill(Color.web("#f2b134"));

        Button loginButton = new Button("Connect");
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #5865f2; -fx-text-fill: white; -fx-font-weight: 700; -fx-pref-height: 42px; -fx-pref-width: 100%;");
        loginButton.setOnAction(event -> handleLogin(stage, usernameField, passwordField, statusLabel));

        VBox fallback = new VBox(6);
        fallback.setPadding(new Insets(12, 0, 0, 0));
        Label fallbackLabel = new Label("Demo credentials: ADMIN/Admin1234, DOCTOR/Doctor1234, NURSE/Nurse1234");
        fallbackLabel.setTextFill(Color.web("#a7b4df"));
        fallbackLabel.setWrapText(true);
        fallback.getChildren().add(fallbackLabel);

        form.getChildren().addAll(userLabel, usernameField, passwordLabel, passwordField, loginButton, statusLabel, fallback);
        cardContent.getChildren().addAll(heading, subText, form);
        card.getChildren().add(cardContent);

        root.getChildren().addAll(serverBar, card);
        return root;
    }

    private void handleLogin(Stage stage, TextField usernameField, PasswordField passwordField, Label statusLabel) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Provide both username and password.");
            return;
        }

        Optional<AppUser> authenticated = tryLogin(username, password);
        if (authenticated.isPresent()) {
            AppUser user = authenticated.get();
            settingsStore.persistLastLogin(user);
            onSuccess.accept(stage, user);
        } else {
            statusLabel.setText("Invalid credentials.");
        }
    }

    private Optional<AppUser> tryLogin(String username, String password) {
        Optional<AppUser> user = authService.authenticate(username, password);
        if (user.isPresent()) {
            return user;
        }
        String lookup = FALLBACK_CREDENTIALS.get(username.toUpperCase());
        if (lookup != null && lookup.equals(password)) {
            return Optional.of(AppUser.withPassword(username.toUpperCase(), "Demo", password));
        }
        return Optional.empty();
    }
}
