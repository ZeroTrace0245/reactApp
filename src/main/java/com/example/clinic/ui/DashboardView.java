package com.example.clinic.ui;

import com.example.clinic.data.UserRepository;
import com.example.clinic.model.AppUser;
import com.example.clinic.model.InventoryItem;
import com.example.clinic.model.PatientRecord;
import com.example.clinic.settings.SettingsStore;
import com.example.clinic.ui.PatientEntryDialog;
import com.example.clinic.util.CsvExporter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class DashboardView {
    private final UserRepository repository;
    private final SettingsStore settingsStore;
    private final ObservableList<AppUser> users = FXCollections.observableArrayList();
    private final ObservableList<PatientRecord> patients = FXCollections.observableArrayList();
    private final ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();
    private final ObservableList<String> statusMessages = FXCollections.observableArrayList();

    private final Text activePatientsValue = new Text();
    private final Text lowInventoryValue = new Text();
    private final Text staffOnDutyValue = new Text();

    private final StackPane mainArea = new StackPane();
    private AppUser loggedIn;
    private Stage primaryStage;
    private TableView<PatientRecord> patientTable;
    private static final List<KeyMetric> OVERVIEW_METRICS = List.of(
            new KeyMetric("Patients", 0.6, "", Color.web("#57F287")),
            new KeyMetric("Inventory", 0.4, "", Color.web("#1abc9c")),
            new KeyMetric("Outstanding", 0.9, "$625.00", Color.web("#f04747")),
            new KeyMetric("Monthly Revenue", 0.55, "$340.00", Color.web("#f3ba4d")),
            new KeyMetric("Open Complaints", 0.2, "", Color.web("#a66df6"))
        );
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Random random = new Random();
    private final Timeline realtimeTimeline = createRealtimeTimeline();
    private final Text navSectionLabel = new Text("Section: Dashboard");
    private Button activeNavButton;
    private static final String NAV_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: #bfc7ff; -fx-font-weight: 600; -fx-border-color: transparent; -fx-pref-width: 172; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.35), 5, 0, 0, 1);";
    private static final String NAV_BUTTON_HOVER = "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #ffffff; -fx-font-weight: 600; -fx-border-color: transparent;";
    private static final String NAV_BUTTON_ACTIVE = "-fx-background-color: rgba(88,101,242,0.45); -fx-text-fill: #ffffff; -fx-font-weight: 600; -fx-border-color: transparent;";

    public DashboardView(UserRepository repository, SettingsStore settingsStore) {
        this.repository = repository;
        this.settingsStore = settingsStore;
        initializeSampleData();
    }

    private Region createValueCard(String title, String value, String accent) {
        Text label = new Text(title);
        label.setFill(Color.web("#9ba4c0"));
        label.setFont(Font.font("Segoe UI", 12));

        Text amount = new Text(value);
        amount.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        amount.setFill(Color.web("#f5f7ff"));

        VBox content = new VBox(6, label, amount);
        content.setAlignment(Pos.CENTER_LEFT);

        Region accentStrip = new Region();
        accentStrip.setPrefHeight(4);
        accentStrip.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 4; -fx-min-width: 100%;");

        VBox combined = new VBox(10, content, accentStrip);
        combined.setPadding(new Insets(10));

        RoundedPane card = new RoundedPane(16, Color.web("#1c2029"));
        card.getChildren().setAll(combined);
        card.setStyle(card.getStyle() + " -fx-background-color: #181b21; -fx-pref-width: 170;");
        return card;
    }

    private Node buildMetricBars() {
        VBox list = new VBox(12);
        for (KeyMetric metric : OVERVIEW_METRICS) {
            list.getChildren().add(createMetricBar(metric));
        }
        return list;
    }

    private Node createMetricBar(KeyMetric metric) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4));
        Text label = new Text(metric.label());
        label.setFill(Color.web("#ced4ff"));
        label.setFont(Font.font("Segoe UI", 12));

        StackPane track = new StackPane();
        track.setPrefHeight(26);
        track.setStyle("-fx-background-color: #151624; -fx-background-radius: 14; -fx-border-radius: 14;");

        Region progress = new Region();
        progress.setStyle("-fx-background-color: " + toHex(metric.accent()) + "; -fx-background-radius: 14;");
        progress.prefWidthProperty().bind(track.widthProperty().multiply(Math.min(metric.ratio(), 1.0)));

        track.getChildren().add(progress);
        HBox.setHgrow(track, Priority.ALWAYS);
        row.getChildren().addAll(label, track);

        if (!metric.value().isEmpty()) {
            Text value = new Text(metric.value());
            value.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            value.setFill(Color.web("#ced4ff"));
            row.getChildren().add(value);
        }
        return row;
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    private record KeyMetric(String label, double ratio, String value, Color accent) {
    }

    private void initializeSampleData() {
        patients.setAll(
                new PatientRecord("Mara Vega", "Stable - Monitoring", "Room 112"),
                new PatientRecord("Hector Liao", "Critical - Ventilated", "Room 204"),
                new PatientRecord("Priya Sen", "Recovery - Physical", "Room 305"),
                new PatientRecord("Elena Brooks", "Observation - Neonatal", "Room 118"),
                new PatientRecord("Ravi Patel", "Pre-op - Clearing", "Room 402")
        );

        inventoryItems.setAll(
                new InventoryItem("Surgical Masks", 320, "Healthy"),
                new InventoryItem("Intravenous Sets", 78, "Reorder soon"),
                new InventoryItem("Standard Syringes", 610, "Healthy"),
                new InventoryItem("Isolation Gowns", 42, "Critical"),
                new InventoryItem("Defibrillator Pads", 6, "Critical")
        );

        statusMessages.setAll(
                "Emergency wing stabilized: 4 beds cleared",
                "Night shift prepping ICU for new admit",
                "Pharmacy flagged 8 meds for audit",
                "Inventory alerts synced with CSV exports",
                "Telemetry: Vitals stream stable across wards",
                "Staff rota refreshed at 21:00"
        );
    }

    public void start(Stage stage, AppUser currentUser) {
        stage.setTitle("Clinic Manager Dashboard");
        stage.setFullScreenExitHint("Press ESC to exit full screen");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #0b0d12;");

        loggedIn = currentUser;
        primaryStage = stage;
        RoundedPane navPad = buildNavPanel();
        root.setLeft(navPad);
        root.setCenter(mainArea);
        root.setRight(createStatusPanel());

        mainArea.prefHeightProperty().bind(root.heightProperty().subtract(32));
        mainArea.prefWidthProperty().bind(root.widthProperty().subtract(navPad.widthProperty()).subtract(32));

        refreshUsers();

        Scene scene = new Scene(root);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        stage.setFullScreen(false);
        realtimeTimeline.play();
        stage.setOnCloseRequest(event -> realtimeTimeline.stop());
        stage.show();
    }

    private VBox buildOverviewView(AppUser currentUser) {
        Text title = new Text("Green Grid - Utilities Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setFill(Color.web("#f5f7ff"));

        Text subtitle = new Text("An eco-minded clinic overview with patient, inventory, and operational insights.");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setFill(Color.web("#9ba4c0"));

        HBox statsRow = new HBox(12,
                createValueCard("Total Patients", String.valueOf(patients.size()), "#57F287"),
                createValueCard("Beds Available", "24", "#1abc9c"),
                createValueCard("Critical Alerts", "2", "#f04747"),
                createValueCard("Open Complaints", "4", "#f3ba4d")
        );
        statsRow.setAlignment(Pos.CENTER_LEFT);

        RoundedPane metricsPanel = createPanel("Key metrics", buildMetricBars());
        VBox.setVgrow(metricsPanel, Priority.NEVER);

        HBox header = buildHeader(currentUser);

        RoundedPane categoryPanel = createPanel("Patient Categories", buildPatientCategoryList());
        VBox.setVgrow(categoryPanel, Priority.NEVER);

        VBox center = new VBox(16, title, subtitle, statsRow, metricsPanel, header, categoryPanel);
        center.setAlignment(Pos.TOP_LEFT);
        center.setPadding(new Insets(0, 0, 0, 10));
        return center;
    }

    private TableView<PatientRecord> buildPatientSummaryTable() {
        TableView<PatientRecord> summaryTable = new TableView<>(patients);
        summaryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<PatientRecord, String> nameColumn = new TableColumn<>("Patient");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<PatientRecord, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        summaryTable.getColumns().setAll(nameColumn, statusColumn);
        summaryTable.setPrefHeight(200);
        summaryTable.setPlaceholder(new Label("No patient records yet"));
        return summaryTable;
    }

    private HBox buildHeader(AppUser currentUser) {
        Text userBanner = new Text("Signed in as " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        userBanner.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        userBanner.setFill(Color.web("#dce0ff"));

        Button addButton = new Button("New Employee");
        addButton.setStyle("-fx-background-color: #5865f2; -fx-text-fill: white; -fx-font-weight: 600;");
        addButton.setOnAction(e -> addEmployee());

        Button exportButton = new Button("Export CSV");
        exportButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: 600;");
        exportButton.setOnAction(e -> exportUsers());

        Button clearSettings = new Button("Clear Settings");
        clearSettings.setStyle("-fx-background-color: #f04747; -fx-text-fill: white; -fx-font-weight: 600;");
        clearSettings.setOnAction(e -> {
            settingsStore.clear();
            info("Settings cleared", "Settings JSON has been reset.");
        });

        HBox actions = new HBox(10, addButton, exportButton, clearSettings);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, userBanner, spacer, actions);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private Node buildPatientsView() {
        patientTable = createDetailedPatientTable();

        Button addButton = new Button("New Patient");
        addButton.setStyle("-fx-background-color: #57f287; -fx-text-fill: #050c18; -fx-font-weight: 600;");
        addButton.setOnAction(e -> addPatientRecord());

        Button editButton = new Button("Edit Patient");
        editButton.setOnAction(e -> editPatientRecord());
        editButton.setStyle("-fx-background-color: #5865f2; -fx-text-fill: white; -fx-font-weight: 600;");

        Button changeRoom = new Button("Change Room");
        changeRoom.setOnAction(e -> changePatientRoom());
        changeRoom.setStyle("-fx-background-color: #f3ba4d; -fx-text-fill: #050c18; -fx-font-weight: 600;");

        Button deleteButton = new Button("Discharge");
        deleteButton.setOnAction(e -> removePatientRecord());
        deleteButton.setStyle("-fx-background-color: #f04747; -fx-text-fill: white; -fx-font-weight: 600;");

        Button exportButton = new Button("Export Patients");
        exportButton.setOnAction(e -> exportPatientRecords());
        exportButton.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: 600;");

        HBox actions = new HBox(10, addButton, editButton, changeRoom, deleteButton, exportButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Text heading = new Text("Patient Dashboard");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.setFill(Color.web("#f5f7ff"));

        VBox layout = new VBox(16, heading, patientTable, actions);
        layout.setPadding(new Insets(10, 0, 0, 10));
        layout.setAlignment(Pos.TOP_LEFT);
        return layout;
    }

    private Node buildInventoryView() {
        Text heading = new Text("Inventory Control");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.setFill(Color.web("#f5f7ff"));

        RoundedPane inventoryPanel = createPanel("Inventory Snapshot", buildInventoryList());
        VBox view = new VBox(16, heading, inventoryPanel);
        view.setPadding(new Insets(10, 0, 0, 10));
        view.setAlignment(Pos.TOP_LEFT);
        return view;
    }

    private Node buildStatusView() {
        Text heading = new Text("Live Status Feed");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.setFill(Color.web("#f5f7ff"));

        RoundedPane statusPanel = createPanel("Status Feed", buildStatusList());
        VBox view = new VBox(16, heading, statusPanel);
        view.setPadding(new Insets(10, 0, 0, 10));
        view.setAlignment(Pos.TOP_LEFT);
        return view;
    }

    private Node buildReportsView() {
        Text heading = new Text("Reports Center");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.setFill(Color.web("#f5f7ff"));

        Label label = new Label("Generate usage, inventory, and patient reports here. Export buttons reuse the patient and user exporters.");
        label.setWrapText(true);
        label.setTextFill(Color.web("#cfd6ff"));

        VBox view = new VBox(14, heading, label);
        view.setPadding(new Insets(10, 0, 0, 10));
        view.setAlignment(Pos.TOP_LEFT);
        return view;
    }

    private Node buildSettingsView() {
        Text heading = new Text("Settings & Utilities");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.setFill(Color.web("#f5f7ff"));

        Button clearSettingsButton = new Button("Clear Saved Settings");
        clearSettingsButton.setStyle("-fx-background-color: #f04747; -fx-text-fill: white; -fx-font-weight: 600;");
        clearSettingsButton.setOnAction(e -> {
            settingsStore.clear();
            info("Settings cleared", "Settings JSON has been reset.");
        });

        VBox view = new VBox(16, heading, clearSettingsButton);
        view.setPadding(new Insets(10, 0, 0, 10));
        view.setAlignment(Pos.TOP_LEFT);
        return view;
    }

    private RoundedPane buildNavPanel() {
        Text brand = new Text("Clinic Club");
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        brand.setFill(Color.web("#f5f7ff"));

        Text tagline = new Text("Operations - Inventory - Patients");
        tagline.setFont(Font.font("Segoe UI", 11));
        tagline.setFill(Color.web("#8f96ba"));

        Separator separator = new Separator();

        Button dashboardBtn = createNavButton("dashboard", "Dashboard");
        Button patientsBtn = createNavButton("patients", "Patients");
        Button inventoryBtn = createNavButton("inventory", "Inventory");
        Button statusBtn = createNavButton("status", "Status Feed");
        Button reportsBtn = createNavButton("reports", "Reports");
        Button settingsBtn = createNavButton("settings", "Settings");

        VBox navButtons = new VBox(8, dashboardBtn, patientsBtn, inventoryBtn, statusBtn, reportsBtn, settingsBtn);
        navButtons.setFillWidth(true);

        VBox navContent = new VBox(16, brand, tagline, separator, navButtons, createStatusBadge());
        navContent.setPadding(new Insets(12));
        navContent.setSpacing(14);

        navSectionLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        navSectionLabel.setFill(Color.web("#9aa4d3"));
        navContent.getChildren().add(navSectionLabel);

        RoundedPane nav = new RoundedPane(26, Color.web("#16181f"));
        nav.getChildren().setAll(navContent);
        nav.setMaxWidth(220);
        activateNav(dashboardBtn, "Dashboard");
        navigateTo("dashboard");
        return nav;
    }

    private Node createStatusBadge() {
        HBox badge = new HBox(6);
        badge.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setPrefSize(10, 10);
        dot.setStyle("-fx-background-color: #57F287; -fx-background-radius: 5;");
        Text status = new Text("Operational");
        status.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        status.setFill(Color.web("#cfd6ff"));
        badge.getChildren().addAll(dot, status);
        return badge;
    }

    private Button createNavButton(String id, String label) {
        Button button = new Button(label);
        button.setId(id);
        button.setStyle(NAV_BUTTON_STYLE);
        button.setOnAction(e -> {
            activateNav(button, label);
            navigateTo(id);
        });
        button.setOnMouseEntered(e -> {
            if (button != activeNavButton) {
                button.setStyle(NAV_BUTTON_HOVER);
            }
        });
        button.setOnMouseExited(e -> {
            if (button != activeNavButton) {
                button.setStyle(NAV_BUTTON_STYLE);
            }
        });
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private void navigateTo(String id) {
        Node target = switch (id) {
            case "patients" -> buildPatientsView();
            case "inventory" -> buildInventoryView();
            case "status" -> buildStatusView();
            case "reports" -> buildReportsView();
            case "settings" -> buildSettingsView();
            default -> buildOverviewView(loggedIn);
        };
        mainArea.getChildren().setAll(target);
    }

    private void activateNav(Button button, String label) {
        if (activeNavButton != null) {
            activeNavButton.setStyle(NAV_BUTTON_STYLE);
        }
        activeNavButton = button;
        activeNavButton.setStyle(NAV_BUTTON_ACTIVE);
        navSectionLabel.setText("Section: " + label);
    }

    private TableView<PatientRecord> createDetailedPatientTable() {
        TableView<PatientRecord> table = new TableView<>(patients);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PatientRecord, String> nameColumn = new TableColumn<>("Patient");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<PatientRecord, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<PatientRecord, String> roomColumn = new TableColumn<>("Room");
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("room"));

        table.getColumns().addAll(nameColumn, statusColumn, roomColumn);
        table.setPlaceholder(new Label("No patient records yet"));
        table.setPrefHeight(280);
        return table;
    }

    private void addPatientRecord() {
        if (primaryStage == null) {
            return;
        }
        Optional<PatientRecord> entry = PatientEntryDialog.request(primaryStage, null);
        entry.ifPresent(patient -> {
            patients.add(patient);
            statusMessages.add(0, "Admitted " + patient.getName() + " to " + patient.getRoom() + ".");
            if (statusMessages.size() > 12) {
                statusMessages.remove(statusMessages.size() - 1);
            }
            refreshStats();
            info("Patient created", patient.getName() + " has been admitted.");
        });
    }

    private void editPatientRecord() {
        if (patientTable == null) {
            return;
        }
        PatientRecord selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Select patient", "Please choose a patient first.");
            return;
        }
        Optional<PatientRecord> updated = PatientEntryDialog.request(primaryStage, selected);
        if (updated.isEmpty()) {
            return;
        }
        PatientRecord updatedRecord = updated.get();
        int index = patients.indexOf(selected);
        if (index >= 0) {
            patients.set(index, updatedRecord);
            refreshStats();
            info("Patient updated", "Record for " + updatedRecord.getName() + " has been updated.");
        }
    }

    private void changePatientRoom() {
        if (patientTable == null) {
            return;
        }
        PatientRecord selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Select patient", "Choose a patient to move.");
            return;
        }
        Optional<String> roomOpt = PromptDialog.request("Change Room", "Room", selected.getRoom());
        if (roomOpt.isEmpty()) {
            return;
        }
        PatientRecord updated = new PatientRecord(selected.getName(), selected.getStatus(), roomOpt.get().trim());
        int index = patients.indexOf(selected);
        if (index >= 0) {
            patients.set(index, updated);
        }
        refreshStats();
        statusMessages.add(0, updated.getName() + " moved to " + updated.getRoom() + ".");
        if (statusMessages.size() > 8) {
            statusMessages.remove(statusMessages.size() - 1);
        }
        info("Room updated", updated.getName() + " is now in " + updated.getRoom() + ".");
    }

    private void removePatientRecord() {
        if (patientTable == null) {
            return;
        }
        PatientRecord selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Select patient", "Choose a patient to discharge.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Discharge patient");
        confirm.setHeaderText("Confirm discharge");
        confirm.setContentText("Are you sure you want to discharge " + selected.getName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                patients.remove(selected);
                refreshStats();
                statusMessages.add(0, selected.getName() + " discharged.");
                if (statusMessages.size() > 8) {
                    statusMessages.remove(statusMessages.size() - 1);
                }
            }
        });
    }

    private void exportPatientRecords() {
        try {
            Path exported = CsvExporter.exportPatients(patients);
            info("Export completed", "Data exported to " + exported.toAbsolutePath());
        } catch (IOException e) {
            error("Export failed", "Unable to write patients CSV: " + e.getMessage());
        }
    }

    private ListView<InventoryItem> buildInventoryList() {
        ListView<InventoryItem> inventoryList = new ListView<>(inventoryItems);
        inventoryList.setPrefHeight(150);
        inventoryList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(InventoryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.getQuantity() + " units - " + item.getStatus());
                }
            }
        });
        return inventoryList;
    }

    private Node buildPatientCategoryList() {
        VBox list = new VBox(6);
        list.setPadding(new Insets(4));
        Map<String, Long> categories = categorizePatients();
        categories.forEach((category, count) -> {
            Label badge = new Label(category + ": " + count);
            badge.setTextFill(Color.web("#e1e4ff"));
            list.getChildren().add(badge);
        });
        return list;
    }

    private Map<String, Long> categorizePatients() {
        return patients.stream()
                .collect(Collectors.groupingBy(p -> categorizeStatus(p.getStatus()), Collectors.counting()));
    }

    private String categorizeStatus(String status) {
        String normalized = status.toLowerCase();
        if (normalized.contains("critical")) {
            return "Critical";
        }
        if (normalized.contains("recovery")) {
            return "Recovery";
        }
        if (normalized.contains("observation")) {
            return "Observation";
        }
        if (normalized.contains("pre-op") || normalized.contains("preop")) {
            return "Pre-op";
        }
        return "Stable";
    }

    private ListView<String> buildStatusList() {
        ListView<String> statusList = new ListView<>(statusMessages);
        statusList.setPrefHeight(220);
        statusList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
        return statusList;
    }

    private RoundedPane createStatusPanel() {
        RoundedPane panel = createPanel("Status Feed", buildStatusList());
        panel.setMaxWidth(250);
        return panel;
    }

    private RoundedPane createPanel(String title, Node content) {
        Text label = new Text(title);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        label.setFill(Color.web("#ced4ff"));

        VBox container = new VBox(10, label, content);
        container.setAlignment(Pos.TOP_LEFT);
        container.setFillWidth(true);

        RoundedPane pane = new RoundedPane(20, Color.web("#1c2029"));
        pane.getChildren().setAll(container);
        return pane;
    }

    private RoundedPane createStatCard(String title, Text value, String accentColor) {
        Text label = new Text(title);
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 12));
        label.setFill(Color.web("#8a95c2"));

        value.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        value.setFill(Color.web("#f9fbff"));

        Region accent = new Region();
        accent.setPrefHeight(4);
        accent.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 3;");

        VBox cardContent = new VBox(6, label, value, accent);
        cardContent.setAlignment(Pos.CENTER_LEFT);
        cardContent.setPadding(new Insets(4));

        RoundedPane card = new RoundedPane(18, Color.web("#1a1d26"));
        card.getChildren().setAll(cardContent);
        card.setPrefWidth(200);
        return card;
    }

    private void refreshUsers() {
        users.setAll(repository.findAll());
        refreshStats();
    }

    private Timeline createRealtimeTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> refreshLiveData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    private void refreshLiveData() {
        if (patients.isEmpty()) {
            return;
        }
        int index = random.nextInt(patients.size());
        PatientRecord selected = patients.get(index);
        List<String> options = List.of("Stable - Monitoring", "Critical - ICU", "Recovery - Therapy", "Observation - Cardio", "Pre-op - Prep");
        String status = options.get(random.nextInt(options.size()));
        PatientRecord updated = new PatientRecord(selected.getName(), status, selected.getRoom());
        patients.set(index, updated);
        statusMessages.add(0, TIME_FORMATTER.format(LocalTime.now()) + " - " + updated.getName() + " status refreshed.");
        if (statusMessages.size() > 12) {
            statusMessages.remove(statusMessages.size() - 1);
        }
        refreshStats();
    }

    private void refreshStats() {
        activePatientsValue.setText(String.valueOf(patients.size()));
        long lowStockCount = inventoryItems.stream()
                .filter(item -> item.getQuantity() < 100)
                .count();
        lowInventoryValue.setText(lowStockCount + " critical items");
        staffOnDutyValue.setText(String.valueOf(users.size()));
    }

    private void addEmployee() {
        Optional<String> username = PromptDialog.request("New Employee", "Username", "");
        if (username.isEmpty() || username.get().trim().isEmpty()) {
            return;
        }
        Optional<String> role = PromptDialog.request("New Employee", "Role", "Staff");
        if (role.isEmpty() || role.get().trim().isEmpty()) {
            return;
        }
        Optional<String> password = PromptDialog.request("New Employee", "Password", "Secret123");
        if (password.isEmpty() || password.get().trim().isEmpty()) {
            return;
        }
        AppUser newUser = AppUser.withPassword(username.get().trim().toUpperCase(), role.get().trim(), password.get().trim());
        repository.save(newUser);
        refreshUsers();
        statusMessages.add(0, "User " + newUser.getUsername() + " joined the roster.");
        if (statusMessages.size() > 8) {
            statusMessages.remove(statusMessages.size() - 1);
        }
        info("Employee created", "User " + newUser.getUsername() + " is now part of the team.");
    }

    private void exportUsers() {
        try {
            Path exported = CsvExporter.exportUsers(repository.findAll());
            info("Export completed", "Data exported to " + exported.toAbsolutePath());
        } catch (IOException e) {
            error("Export failed", "Unable to write CSV: " + e.getMessage());
        }
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
