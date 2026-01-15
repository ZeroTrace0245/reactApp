package com.example.clinic.util;

import com.example.clinic.model.AppUser;
import com.example.clinic.model.PatientRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class CsvExporter {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private CsvExporter() {
        // Utility class
    }

    public static Path exportUsers(List<AppUser> users) throws IOException {
        Path exports = Path.of("exports");
        Files.createDirectories(exports);
        String filename = "users-" + LocalDateTime.now().format(FORMATTER) + ".csv";
        Path target = exports.resolve(filename);
        try (var writer = Files.newBufferedWriter(target)) {
            writer.write("Username,Role,PasswordHash\n");
            for (AppUser user : users) {
                writer.write(String.format("%s,%s,%s\n", user.getUsername(), user.getRole(), user.getPasswordHash()));
            }
        }
        return target;
    }

    public static Path exportPatients(List<PatientRecord> patients) throws IOException {
        Path exports = Path.of("exports");
        Files.createDirectories(exports);
        String filename = "patients-" + LocalDateTime.now().format(FORMATTER) + ".csv";
        Path target = exports.resolve(filename);
        try (var writer = Files.newBufferedWriter(target)) {
            writer.write("Name,Status,Room\n");
            for (PatientRecord patient : patients) {
                writer.write(String.format("%s,%s,%s\n", patient.getName(), patient.getStatus(), patient.getRoom()));
            }
        }
        return target;
    }

    public static Path exportStatusSnapshot(List<PatientRecord> patients) throws IOException {
        Path exports = Path.of("exports");
        Files.createDirectories(exports);
        LocalDateTime now = LocalDateTime.now();
        String filename = "status-" + now.format(FORMATTER) + ".csv";
        Path target = exports.resolve(filename);
        try (var writer = Files.newBufferedWriter(target)) {
            writer.write("Name,Status,Room,CapturedAt\n");
            for (PatientRecord patient : patients) {
                writer.write(String.format("%s,%s,%s,%s\n",
                        patient.getName(),
                        patient.getStatus(),
                        patient.getRoom(),
                        now));
            }
        }
        return target;
    }
}
