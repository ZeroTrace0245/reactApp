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
import javafx.scene.layout.Priority;
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
        Scene scene = new Scene(createContent(stage), 840, 520);
        applyStyles(scene);
        stage.setScene(scene);
        stage.show();
    }

    private Parent createContent(Stage stage) {
        HBox root = new HBox(32);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("login-root");

        VBox hero = new VBox(12);
        hero.setAlignment(Pos.CENTER_LEFT);
        Text brand = new Text("Clinic Manager");
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        brand.getStyleClass().add("heading-text");
        Text tagline = new Text("Secure access to patients, inventory, and staff in one workspace.");
        tagline.getStyleClass().add("subheading-text");
        tagline.setWrappingWidth(360);

        HBox tags = new HBox(8);
        tags.setAlignment(Pos.CENTER_LEFT);
        for (String label : new String[]{"Patients", "Inventory", "Staff"}) {
            Label chip = new Label(label);
            chip.getStyleClass().add("pill-badge");
            tags.getChildren().add(chip);
        }

        VBox highlights = new VBox(6);
        highlights.setAlignment(Pos.CENTER_LEFT);
        for (String point : new String[]{
                "Role-based access for admin, doctor, nurse",
                "Live status feed and low-stock alerts",
                "One-click CSV exports for audits"
        }) {
            Label bullet = new Label("- " + point);
            bullet.getStyleClass().add("hero-bullet");
            highlights.getChildren().add(bullet);
        }

        hero.getChildren().addAll(brand, tagline, tags, highlights);

        RoundedPane card = new RoundedPane(26, Color.web("#161b23"));
        card.setMaxWidth(420);
        card.getStyleClass().add("login-card");

        VBox cardContent = new VBox(16);
        cardContent.setAlignment(Pos.CENTER_LEFT);
        Text heading = new Text("Welcome back");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        heading.getStyleClass().add("heading-text");
        Text subText = new Text("Sign in to continue");
        subText.getStyleClass().add("subheading-text");
        subText.setFont(Font.font(14));

        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setFillWidth(true);

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("muted-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setText(settingsStore.getLastUsername().orElse(""));
        usernameField.getStyleClass().add("input-field");
        usernameField.setMaxWidth(Double.MAX_VALUE);

        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("muted-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("input-field");
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-warning");

        Button loginButton = new Button("Sign in");
        loginButton.setDefaultButton(true);
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> handleLogin(stage, usernameField, passwordField, statusLabel));

        VBox fallback = new VBox(6);
        fallback.setPadding(new Insets(10, 0, 0, 0));
        Label fallbackLabel = new Label("Demo credentials: ADMIN/Admin1234, DOCTOR/Doctor1234, NURSE/Nurse1234");
        fallbackLabel.getStyleClass().add("muted-text");
        fallbackLabel.setWrapText(true);
        fallback.getChildren().add(fallbackLabel);

        form.getChildren().addAll(userLabel, usernameField, passwordLabel, passwordField, loginButton, statusLabel, fallback);
        cardContent.getChildren().addAll(heading, subText, form);
        card.getChildren().add(cardContent);

        root.getChildren().addAll(hero, card);
        HBox.setHgrow(card, Priority.NEVER);
        HBox.setHgrow(hero, Priority.ALWAYS);
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

    private void applyStyles(Scene scene) {
        var css = getClass().getResource("/styles/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
