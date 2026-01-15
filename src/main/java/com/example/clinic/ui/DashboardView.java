package com.example.clinic.ui;

import com.example.clinic.data.UserRepository;
import com.example.clinic.model.AppUser;
import com.example.clinic.model.InventoryItem;
import com.example.clinic.model.PatientRecord;
import com.example.clinic.model.Appointment;
import com.example.clinic.settings.SettingsStore;
import com.example.clinic.ui.PatientEntryDialog;
import com.example.clinic.ui.InventoryEntryDialog;
import com.example.clinic.ui.AppointmentEntryDialog;
import com.example.clinic.util.CsvExporter;
import javafx.animation.KeyFrame;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ScrollPane;
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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class DashboardView {
    private final UserRepository repository;
    private final SettingsStore settingsStore;
    private final Runnable logoutAction;
    private final ObservableList<AppUser> users = FXCollections.observableArrayList();
    private final ObservableList<PatientRecord> patients = FXCollections.observableArrayList();
    private final ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final ObservableList<String> statusMessages = FXCollections.observableArrayList();

    private final Text activePatientsValue = new Text();
    private final Text lowInventoryValue = new Text();
    private final Text staffOnDutyValue = new Text();

    private final StackPane mainArea = new StackPane();
    private Button activeNavButton;
    private AppUser loggedIn;
    private Stage primaryStage;
    private TableView<PatientRecord> patientTable;
    private TableView<InventoryItem> inventoryTable;
    private TableView<Appointment> appointmentTable;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter APPT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final Random random = new Random();
    private final Timeline realtimeTimeline = createRealtimeTimeline();

    public DashboardView(UserRepository repository, SettingsStore settingsStore, Runnable logoutAction) {
        this.repository = repository;
        this.settingsStore = settingsStore;
        this.logoutAction = logoutAction;
        initializeSampleData();
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
                new InventoryItem("INV-420115", "Surgical Masks", 320, 0.35, "Healthy"),
                new InventoryItem("INV-420116", "Intravenous Sets", 78, 4.80, "Reorder soon"),
                new InventoryItem("INV-420117", "Standard Syringes", 610, 0.85, "Healthy"),
                new InventoryItem("INV-420118", "Isolation Gowns", 42, 6.25, "Critical"),
                new InventoryItem("INV-420119", "Defibrillator Pads", 6, 12.00, "Critical")
        );

        appointments.setAll(
                new Appointment("APT-301100", "Mara Vega", "Dr. Carter", LocalDateTime.now().plusHours(3), "Post-op check"),
                new Appointment("APT-301101", "Hector Liao", "Nurse Samuels", LocalDateTime.now().plusHours(6), "Ventilator check"),
                new Appointment("APT-301102", "Priya Sen", "Dr. Ortega", LocalDateTime.now().plusDays(1), "Therapy consult")
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
        stage.setTitle("Clinic Manager");
        stage.setFullScreenExitHint("Press ESC to exit full screen");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("app-root");

        loggedIn = currentUser;
        primaryStage = stage;

        VBox mainContent = new VBox(18);
        mainContent.setPadding(new Insets(4, 6, 20, 6));
        mainContent.getChildren().addAll(
                buildHeroSection(),
                buildNavBar(),
                mainArea
        );
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        navigateTo("overview");

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.setCenter(scrollPane);

        Button logoutFab = new Button("Logout");
        logoutFab.getStyleClass().add("secondary-button");
        logoutFab.setOnAction(e -> logout());
        HBox bottomBar = new HBox(logoutFab);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(8, 0, 0, 4));
        root.setBottom(bottomBar);

        refreshUsers();

        Scene scene = new Scene(root);
        applyStyles(scene);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setResizable(true);
        realtimeTimeline.play();
        stage.setOnCloseRequest(event -> realtimeTimeline.stop());
        stage.show();
    }

    private HBox buildNavBar() {
        Button overviewBtn = createNavButton("overview", "Overview");
        Button patientsBtn = createNavButton("patients", "Patients");
        Button appointmentsBtn = createNavButton("appointments", "Appointments");
        Button inventoryBtn = createNavButton("inventory", "Inventory");
        Button statusBtn = createNavButton("status", "Status");
        Button reportsBtn = createNavButton("reports", "Reports");
        Button settingsBtn = createNavButton("settings", "Settings");

        HBox nav = new HBox(10, overviewBtn, patientsBtn, appointmentsBtn, inventoryBtn, statusBtn, reportsBtn, settingsBtn);
        nav.setAlignment(Pos.CENTER_LEFT);
        activateNav(overviewBtn);
        return nav;
    }

    private Button createNavButton(String id, String label) {
        Button button = new Button(label);
        button.setId(id);
        button.getStyleClass().add("nav-button");
        button.setOnAction(e -> {
            activateNav(button);
            navigateTo(id);
        });
        return button;
    }

    private void activateNav(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }
        activeNavButton = button;
        if (!activeNavButton.getStyleClass().contains("active")) {
            activeNavButton.getStyleClass().add("active");
        }
    }

    private void navigateTo(String id) {
        Node target = switch (id) {
            case "patients" -> buildPatientsView();
            case "appointments" -> buildAppointmentsView();
            case "inventory" -> buildInventoryView();
            case "status" -> buildStatusView();
            case "reports" -> buildReportsView();
            case "settings" -> buildSettingsView();
            default -> buildOverviewView();
        };
        mainArea.getChildren().setAll(target);
        FadeTransition fade = new FadeTransition(Duration.millis(250), target);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private RoundedPane buildHeroSection() {
        Text title = new Text("Clinic Control Center");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.getStyleClass().add("section-heading");

        Text subtitle = new Text("Unified patients, inventory, and staff overview in one clean view.");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.getStyleClass().add("section-subheading");

        Text userBanner = new Text("Signed in as " + loggedIn.getUsername() + " (" + loggedIn.getRole() + ")");
        userBanner.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        userBanner.getStyleClass().add("section-subheading");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button addEmployee = new Button("New Employee");
        addEmployee.getStyleClass().add("primary-button");
        addEmployee.setOnAction(e -> addEmployee());

        Button exportUsers = new Button("Export Users");
        exportUsers.getStyleClass().add("secondary-button");
        exportUsers.setOnAction(e -> exportUsers());

        Button clearSettings = new Button("Clear Settings");
        clearSettings.getStyleClass().add("danger-button");
        clearSettings.setOnAction(e -> {
            settingsStore.clear();
            info("Settings cleared", "Settings JSON has been reset.");
        });

        actions.getChildren().addAll(addEmployee, exportUsers, clearSettings);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, spacer, actions);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(10, header, subtitle, userBanner);
        container.setAlignment(Pos.TOP_LEFT);

        RoundedPane pane = new RoundedPane(22, Color.web("#16181f"));
        pane.getChildren().setAll(container);
        pane.getStyleClass().add("hero-pane");
        return pane;
    }

    private Node buildStatsRow() {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
                createStatCard("Active Patients", activePatientsValue, "#57F287"),
                createStatCard("Low Inventory", lowInventoryValue, "#f3ba4d"),
                createStatCard("Staff On Duty", staffOnDutyValue, "#5865f2")
        );
        return row;
    }

    private Node buildOverviewView() {
        VBox view = new VBox(16,
                buildStatsRow(),
                buildContentGrid()
        );
        view.setAlignment(Pos.TOP_LEFT);
        return view;
    }

    private Node buildContentGrid() {
        VBox leftColumn = new VBox(14,
                buildPatientsView(),
                buildAppointmentsView(),
                buildStatusView()
        );

        VBox rightColumn = new VBox(14,
                buildInventoryView(),
                buildReportsView(),
                buildSettingsView()
        );

        HBox grid = new HBox(14, leftColumn, rightColumn);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.SOMETIMES);
        leftColumn.setPrefWidth(720);
        rightColumn.setPrefWidth(420);
        return grid;
    }

    private Node buildPatientsView() {
        patientTable = createDetailedPatientTable();

        Button addButton = new Button("New Patient");
        addButton.getStyleClass().add("success-button");
        addButton.setOnAction(e -> addPatientRecord());

        Button editButton = new Button("Edit Patient");
        editButton.setOnAction(e -> editPatientRecord());
        editButton.getStyleClass().add("primary-button");

        Button changeRoom = new Button("Change Room");
        changeRoom.setOnAction(e -> changePatientRoom());
        changeRoom.getStyleClass().add("warning-button");

        Button deleteButton = new Button("Discharge");
        deleteButton.setOnAction(e -> removePatientRecord());
        deleteButton.getStyleClass().add("danger-button");

        Button exportButton = new Button("Export Patients");
        exportButton.setOnAction(e -> exportPatientRecords());
        exportButton.getStyleClass().add("secondary-button");

        HBox actions = new HBox(10, addButton, editButton, changeRoom, deleteButton, exportButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Text heading = new Text("Patient Dashboard");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.getStyleClass().add("section-heading");

        VBox layout = new VBox(12, heading, patientTable, actions);
        layout.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(patientTable, Priority.ALWAYS);
        return createSectionCard(layout);
    }

    private Node buildAppointmentsView() {
        appointmentTable = createAppointmentTable();

        Button addButton = new Button("New Appointment");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> addAppointment());

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(e -> editAppointment());

        Button deleteButton = new Button("Cancel");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(e -> removeAppointment());

        HBox actions = new HBox(10, addButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Text heading = new Text("Appointments");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.getStyleClass().add("section-heading");

        VBox layout = new VBox(12, heading, appointmentTable, actions);
        layout.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(appointmentTable, Priority.ALWAYS);
        return createSectionCard(layout);
    }

    private Node buildInventoryView() {
        Text heading = new Text("Inventory Control");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.getStyleClass().add("section-heading");

        inventoryTable = createInventoryTable();

        Button addButton = new Button("Add Item");
        addButton.getStyleClass().add("primary-button");
        addButton.setOnAction(e -> addInventoryItem());

        Button editButton = new Button("Edit Item");
        editButton.getStyleClass().add("secondary-button");
        editButton.setOnAction(e -> editInventoryItem());

        Button deleteButton = new Button("Remove");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.setOnAction(e -> removeInventoryItem());

        HBox actions = new HBox(10, addButton, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox view = new VBox(12, heading, inventoryTable, actions);
        view.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        return createSectionCard(view);
    }

    private Node buildStatusView() {
        Text heading = new Text("Live Status Feed");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.getStyleClass().add("section-heading");

        Button exportStatus = new Button("Export Status Snapshot");
        exportStatus.getStyleClass().add("secondary-button");
        exportStatus.setOnAction(e -> exportStatusSnapshot());

        VBox view = new VBox(12, heading, buildStatusList(), exportStatus);
        view.setAlignment(Pos.TOP_LEFT);
        return createSectionCard(view);
    }

    private Node buildReportsView() {
        Text heading = new Text("Reports Center");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.getStyleClass().add("section-heading");

        Label label = new Label("Generate usage, inventory, and patient reports here. Export buttons reuse the patient and user exporters.");
        label.setWrapText(true);
        label.getStyleClass().add("muted-text");

        Button generateButton = new Button("Generate Reports");
        generateButton.getStyleClass().add("primary-button");
        generateButton.setOnAction(e -> generateReports());

        VBox view = new VBox(10, heading, label, generateButton);
        view.setAlignment(Pos.TOP_LEFT);
        return createSectionCard(view);
    }

    private void generateReports() {
        try {
            Path patientsPath = CsvExporter.exportPatients(patients);
            Path usersPath = CsvExporter.exportUsers(repository.findAll());
            info("Reports generated", "Patients: " + patientsPath.toAbsolutePath() + "\nUsers: " + usersPath.toAbsolutePath());
        } catch (IOException e) {
            error("Reports failed", "Unable to generate reports: " + e.getMessage());
        }
    }

    private Node buildSettingsView() {
        Text heading = new Text("Settings & Utilities");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.getStyleClass().add("section-heading");

        Button clearSettingsButton = new Button("Clear Saved Settings");
        clearSettingsButton.getStyleClass().add("danger-button");
        clearSettingsButton.setOnAction(e -> {
            settingsStore.clear();
            info("Settings cleared", "Settings JSON has been reset.");
        });

        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.getStyleClass().add("warning-button");
        resetPasswordButton.setOnAction(e -> resetPassword());

        Button logoutButton = new Button("Log out");
        logoutButton.getStyleClass().add("secondary-button");
        logoutButton.setOnAction(e -> logout());

        VBox view = new VBox(12, heading, clearSettingsButton, resetPasswordButton, logoutButton);
        view.setAlignment(Pos.TOP_LEFT);
        return createSectionCard(view);
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

    private TableView<Appointment> createAppointmentTable() {
        TableView<Appointment> table = new TableView<>(appointments);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Appointment, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Appointment, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));

        TableColumn<Appointment, String> clinicianColumn = new TableColumn<>("Clinician");
        clinicianColumn.setCellValueFactory(new PropertyValueFactory<>("clinician"));

        TableColumn<Appointment, LocalDateTime> timeColumn = new TableColumn<>("Scheduled");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("scheduledAt"));
        timeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.format(APPT_FORMATTER));
            }
        });

        TableColumn<Appointment, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        table.getColumns().addAll(idColumn, patientColumn, clinicianColumn, timeColumn, notesColumn);
        table.setPlaceholder(new Label("No appointments scheduled"));
        table.setPrefHeight(220);
        return table;
    }

    private TableView<InventoryItem> createInventoryTable() {
        TableView<InventoryItem> table = new TableView<>(inventoryItems);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<InventoryItem, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<InventoryItem, String> nameColumn = new TableColumn<>("Item");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<InventoryItem, Integer> quantityColumn = new TableColumn<>("Qty");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<InventoryItem, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });

        TableColumn<InventoryItem, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idColumn, nameColumn, quantityColumn, priceColumn, statusColumn);
        table.setPlaceholder(new Label("No inventory items yet"));
        table.setPrefHeight(220);
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

    private void addAppointment() {
        if (!isClinician()) {
            info("Restricted", "Only doctors or nurses can create appointments.");
            return;
        }
        Optional<Appointment> result = AppointmentEntryDialog.request(primaryStage, null, this::generateAppointmentId, loggedIn.getUsername());
        result.ifPresent(appt -> {
            appointments.add(0, appt);
            statusMessages.add(0, "Scheduled appointment " + appt.getId() + " for " + appt.getPatientName() + ".");
            if (statusMessages.size() > 12) {
                statusMessages.remove(statusMessages.size() - 1);
            }
            info("Appointment created", "Appointment set for " + appt.getPatientName() + ".");
        });
    }

    private void editAppointment() {
        if (!isClinician()) {
            info("Restricted", "Only doctors or nurses can edit appointments.");
            return;
        }
        Appointment selected = appointmentTable == null ? null : appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Select appointment", "Choose an appointment first.");
            return;
        }
        Optional<Appointment> result = AppointmentEntryDialog.request(primaryStage, selected, () -> selected.getId(), loggedIn.getUsername());
        result.ifPresent(appt -> {
            int index = appointments.indexOf(selected);
            if (index >= 0) {
                appointments.set(index, appt);
                statusMessages.add(0, "Updated appointment " + appt.getId() + ".");
                if (statusMessages.size() > 12) {
                    statusMessages.remove(statusMessages.size() - 1);
                }
                info("Appointment updated", "Appointment " + appt.getId() + " saved.");
            }
        });
    }

    private void removeAppointment() {
        if (!isClinician()) {
            info("Restricted", "Only doctors or nurses can cancel appointments.");
            return;
        }
        Appointment selected = appointmentTable == null ? null : appointmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Select appointment", "Choose an appointment to cancel.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel appointment");
        confirm.setHeaderText("Confirm cancellation");
        confirm.setContentText("Cancel " + selected.getId() + " for " + selected.getPatientName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                appointments.remove(selected);
                statusMessages.add(0, "Cancelled appointment " + selected.getId() + ".");
                if (statusMessages.size() > 12) {
                    statusMessages.remove(statusMessages.size() - 1);
                }
            }
        });
    }

    private boolean isClinician() {
        if (loggedIn == null) {
            return false;
        }
        String role = loggedIn.getRole().toLowerCase();
        return role.contains("doctor") || role.contains("nurse");
    }

    private String generateAppointmentId() {
        return "APT-" + (300000 + random.nextInt(90000));
    }

    private void addInventoryItem() {
        if (primaryStage == null) {
            return;
        }
        Optional<InventoryItem> entry = InventoryEntryDialog.request(primaryStage, null, this::generateInventoryId);
        entry.ifPresent(item -> {
            inventoryItems.add(0, item);
            refreshStats();
            statusMessages.add(0, "Added inventory item " + item.getName() + " (" + item.getId() + ").");
            if (statusMessages.size() > 12) {
                statusMessages.remove(statusMessages.size() - 1);
            }
            info("Item added", item.getName() + " has been added to inventory.");
        });
    }

    private void editInventoryItem() {
        if (inventoryTable == null) {
            return;
        }
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Select item", "Please choose an inventory item first.");
            return;
        }
        Optional<InventoryItem> updated = InventoryEntryDialog.request(primaryStage, selected, () -> selected.getId());
        updated.ifPresent(item -> {
            int index = inventoryItems.indexOf(selected);
            if (index >= 0) {
                inventoryItems.set(index, item);
                refreshStats();
                statusMessages.add(0, "Updated " + item.getName() + " (" + item.getId() + ").");
                if (statusMessages.size() > 12) {
                    statusMessages.remove(statusMessages.size() - 1);
                }
                info("Inventory updated", "Item " + item.getName() + " saved.");
            }
        });
    }

    private void removeInventoryItem() {
        if (inventoryTable == null) {
            return;
        }
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            info("Select item", "Choose an inventory item to remove.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove item");
        confirm.setHeaderText("Confirm removal");
        confirm.setContentText("Remove " + selected.getName() + " from inventory?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                inventoryItems.remove(selected);
                refreshStats();
                statusMessages.add(0, selected.getName() + " removed from inventory.");
                if (statusMessages.size() > 12) {
                    statusMessages.remove(statusMessages.size() - 1);
                }
            }
        });
    }

    private String generateInventoryId() {
        return "INV-" + (100000 + random.nextInt(900000));
    }

    private void exportPatientRecords() {
        try {
            Path exported = CsvExporter.exportPatients(patients);
            info("Export completed", "Data exported to " + exported.toAbsolutePath());
        } catch (IOException e) {
            error("Export failed", "Unable to write patients CSV: " + e.getMessage());
        }
    }

    private void exportStatusSnapshot() {
        try {
            Path exported = CsvExporter.exportStatusSnapshot(patients);
            statusMessages.add(0, "Status snapshot exported at " + TIME_FORMATTER.format(LocalTime.now()) + ".");
            if (statusMessages.size() > 12) {
                statusMessages.remove(statusMessages.size() - 1);
            }
            info("Export completed", "Status snapshot saved to " + exported.toAbsolutePath());
        } catch (IOException e) {
            error("Export failed", "Unable to write status snapshot: " + e.getMessage());
        }
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

    private RoundedPane createSectionCard(Node content) {
        RoundedPane card = new RoundedPane(20, Color.web("#1c2029"));
        card.getChildren().setAll(content);
        card.getStyleClass().add("content-card");
        return card;
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
        card.getStyleClass().add("stat-card");
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

    private void resetPassword() {
        if (loggedIn == null) {
            return;
        }
        Optional<String> newPassword = PromptDialog.request("Reset Password", "New Password", "NewPass123");
        if (newPassword.isEmpty() || newPassword.get().trim().isEmpty()) {
            return;
        }
        loggedIn = loggedIn.withNewPassword(newPassword.get().trim());
        repository.save(loggedIn);
        statusMessages.add(0, "Password updated for user " + loggedIn.getUsername() + ".");
        if (statusMessages.size() > 12) {
            statusMessages.remove(statusMessages.size() - 1);
        }
        info("Password updated", "Password updated for " + loggedIn.getUsername());
    }

    private void logout() {
        realtimeTimeline.stop();
        if (logoutAction != null) {
            logoutAction.run();
        }
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("app-dialog");
        alert.showAndWait();
    }

    private void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.getDialogPane().getStyleClass().add("app-dialog-danger");
        alert.showAndWait();
    }

    private void applyStyles(Scene scene) {
        var css = getClass().getResource("/styles/app.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }
}
