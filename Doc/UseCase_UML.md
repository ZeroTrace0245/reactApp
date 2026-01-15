# Clinic Manager — Use Case Diagram

This document describes the main use cases for the Clinic Manager app and maps UI interactions to code components and functions. It includes a PlantUML diagram you can render to see a visual illustration.

---

## Actors

- Doctor (role contains "doctor")
- Nurse (role contains "nurse")
- Admin (role contains "admin")
- Receptionist / Staff (generic staff user)

## High-level Use Cases

- Authenticate (login)
- View Dashboard (overview)
- Manage Patients
  - Add Patient
  - Edit Patient
  - Discharge Patient
  - Change Room
  - Export Patients CSV
- Manage Inventory
  - Add Item
  - Edit Item
  - Remove Item
- Appointments
  - Create Appointment (Doctor/Nurse)
  - Edit Appointment (Doctor/Nurse)
  - Cancel Appointment (Doctor/Nurse)
- Live Status Feed
  - View status messages
  - Export status snapshot (CSV)
- Reports & Utilities
  - Export users
  - Generate reports
  - Reset password
  - Clear settings
- Logout (persistent logout control)

## Code Mapping (UI ? functions/classes)

- Authentication
  - `LoginView.handleLogin` ? uses `AuthService.authenticate` and `AppUser.withPassword` fallback
  - `SettingsStore.persistLastLogin` stores last user

- Dashboard / Navigation
  - `DashboardView.start`, `DashboardView.navigateTo`, `DashboardView.buildNavBar`
  - `DashboardView.buildHeroSection`, `DashboardView.buildStatsRow` show metrics

- Patient management
  - `DashboardView.addPatientRecord` ? `PatientEntryDialog.request` ? creates `PatientRecord`
  - `DashboardView.editPatientRecord` ? `PatientEntryDialog.request` with existing
  - `DashboardView.removePatientRecord` (discharge)
  - `DashboardView.changePatientRoom` ? `PromptDialog.request`
  - `CsvExporter.exportPatients` exports current `patients` list

- Inventory management
  - `DashboardView.addInventoryItem` ? `InventoryEntryDialog.request` ? creates `InventoryItem`
  - `DashboardView.editInventoryItem` / `removeInventoryItem`
  - `DashboardView.createInventoryTable` shows `id`, `name`, `quantity`, `price`, `status`

- Appointments
  - `DashboardView.addAppointment` ? `AppointmentEntryDialog.request` ? creates `Appointment`
  - `DashboardView.editAppointment` / `removeAppointment` (restricted by `DashboardView.isClinician`)
  - `DashboardView.createAppointmentTable` displays appointments

- Status & Exports
  - `DashboardView.buildStatusView` shows `buildStatusList`
  - `DashboardView.exportStatusSnapshot` ? `CsvExporter.exportStatusSnapshot` (writes time + patient statuses)
  - `CsvExporter.exportUsers` and `generateReports` for broader exports

- Dialogs & Theming
  - `PatientEntryDialog`, `InventoryEntryDialog`, `AppointmentEntryDialog`, `PromptDialog` use `app.css` for theme
  - Alerts use `info` / `error` helpers in `DashboardView` (adds dialog CSS classes)

- Logout
  - `DashboardView.logout` invoked from bottom-left `Logout` button and settings view `Log out` button


---

## PlantUML Use Case Diagram

Copy the block below into a PlantUML renderer (online or local) to produce a diagram.

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle

actor Doctor
actor Nurse
actor Admin
actor Receptionist as Staff

rectangle "Clinic Manager" {
  usecase "Login" as UC_Login
  usecase "View Dashboard" as UC_Dashboard
  usecase "Manage Patients" as UC_Patients
  usecase "Manage Inventory" as UC_Inventory
  usecase "Manage Appointments" as UC_Appointments
  usecase "Live Status Feed" as UC_Status
  usecase "Export / Reports" as UC_Reports
  usecase "Logout" as UC_Logout
}

Doctor --> UC_Login
Nurse  --> UC_Login
Admin  --> UC_Login
Staff  --> UC_Login

Doctor --> UC_Dashboard
Nurse  --> UC_Dashboard
Admin  --> UC_Dashboard
Staff  --> UC_Dashboard

Doctor --> UC_Patients
Nurse  --> UC_Patients
Admin  --> UC_Patients

Doctor --> UC_Appointments
Nurse  --> UC_Appointments
Receptionist --> UC_Appointments : schedule

Admin --> UC_Inventory
Staff --> UC_Inventory

Doctor --> UC_Status
Nurse  --> UC_Status
Admin  --> UC_Status

Admin --> UC_Reports

UC_Login .down.> UC_Dashboard : onSuccess
UC_Patients .down.> UC_Reports : export
UC_Status .down.> UC_Reports : export snapshot

note right of UC_Patients
  UI: DashboardView
  Dialog: PatientEntryDialog
  Export: CsvExporter.exportPatients
end note

note right of UC_Inventory
  UI: DashboardView
  Dialog: InventoryEntryDialog
end note

note right of UC_Appointments
  UI: DashboardView
  Dialog: AppointmentEntryDialog
  Restrictions: DashboardView.isClinician()
end note

@enduml
```

---

## ASCII Interaction Overview (quick)

- User opens app ? `LoginView` ? `AuthService` ? on success `DashboardView.start`
- From `DashboardView` user clicks nav ? `navigateTo` loads corresponding view
- Add patient ? `PatientEntryDialog` ? returned `PatientRecord` ? `patients.add(...)`
- Create appointment (doctor/nurse) ? `AppointmentEntryDialog` ? `appointments.add(...)`
- Add inventory ? `InventoryEntryDialog` ? `inventoryItems.add(...)`
- Export status ? `CsvExporter.exportStatusSnapshot(patients)` writes CSV with capture time

---

If you want, I can also add a rendered PNG/SVG of the PlantUML output into the `docs/` folder (requires PlantUML rendering).