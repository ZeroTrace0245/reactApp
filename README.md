# Clinic Manager

A JavaFX desktop dashboard for small clinics to manage users, patient records, inventory, and live status updates. Ships with demo users and sample patient/inventory data so you can explore the UI immediately, and supports CSV export for patients and users.

## What it does
- **Login**: Starts at a credential screen; demo users are created on first launch.
- **Patients**: Add/edit/discharge patients, change rooms, and export patient lists to CSV.
- **Inventory**: View inventory items and quickly see critical/low stock counts.
- **Status feed**: Rolling activity feed with live status refreshes every 5s.
- **Reports**: One-click CSV exports for patients and users.
- **Settings**: Persist/clear last login info stored in a JSON file.

## How it works
- **Entry point**: `com.example.clinic.MainApp` wires the login view and, on success, opens `DashboardView`.
- **Users**: Backed by SQLite in `storage/users.db`; schema is auto-created and demo accounts are ensured on startup.
- **Demo logins** (username ? role ? password): `ADMIN` ? Administrator ? `Admin1234`, `DOCTOR` ? Doctor ? `Doctor1234`, `NURSE` ? Nurse ? `Nurse1234`.
- **Settings**: Last login info persists to `config/settings.json` via `SettingsStore`.
- **Exports**: CSV files are written to the `exports/` folder with timestamped filenames.
- **Styling**: Custom styles live in `src/main/resources/styles/app.css`.

## Technology stack
- Java 21
- JavaFX 22 (controls/graphics) with `javafx-maven-plugin`
- SQLite via `sqlite-jdbc`
- JSON serialization with Jackson
- Maven for build/run

## Prerequisites
- JDK 21 installed and on your `PATH`
- Maven 3.9+ installed (`mvn -v`)

## Setup & build (Maven)
```bash
# clone
git clone <repo-url>
cd Project_Java

# download dependencies and build
mvn clean install
```

## Run the app
Using the JavaFX Maven plugin (recommended during development):
```bash
mvn javafx:run
```

From the built jar (after `mvn clean package`):
```bash
java -jar target/clinic-manager-1.0.0.jar
```
> The JavaFX runtime is pulled from Maven; no extra manual JavaFX install is needed when running with Maven.

## How to use (quick start)
1) Run `mvn javafx:run`.
2) Log in with a demo account (e.g., `ADMIN` / `Admin1234`).
3) Explore patients, add or edit entries, and try CSV export.
4) Add a new user via **New Employee** and confirm it shows in staff count.
5) Generate reports to see CSVs appear in `exports/`.

## Project structure
```
src/main/java/com/example/clinic     # JavaFX views and logic
src/main/resources                   # CSS and resource files
pom.xml                              # Maven build config
```

## Notes
- Patient and user CSV exports land in `exports/`.
- User data persists to `storage/users.db`; settings persist to `config/settings.json`.
- The app starts from `com.example.clinic.MainApp` (configured in `pom.xml`).
