package com.example.clinic;

import com.example.clinic.data.UserRepository;
import com.example.clinic.model.AppUser;
import com.example.clinic.service.AuthService;
import com.example.clinic.settings.SettingsStore;
import com.example.clinic.ui.DashboardView;
import com.example.clinic.ui.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    private final UserRepository repository = new UserRepository();
    private final SettingsStore settingsStore = new SettingsStore();
    private final AuthService authService = new AuthService(repository);

    @Override
    public void start(Stage primaryStage) {
        repository.ensureDemoUsers();
        var loginView = new LoginView(authService, settingsStore, this::showDashboard);
        loginView.start(primaryStage);
    }

    private void showDashboard(Stage stage, AppUser user) {
        Runnable logout = () -> {
            var loginView = new LoginView(authService, settingsStore, this::showDashboard);
            loginView.start(stage);
        };
        var dashboard = new DashboardView(repository, settingsStore, logout);
        dashboard.start(stage, user);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
