package com.example.clinic.data;

import com.example.clinic.model.AppUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {
    private final String jdbcUrl;

    public UserRepository() {
        try {
            Path storage = Paths.get("storage");
            Files.createDirectories(storage);
            Path dbFile = storage.resolve("users.db");
            this.jdbcUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();
            setupSchema();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to prepare user storage", e);
        }
    }

    private void setupSchema() {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS app_user (id TEXT PRIMARY KEY, username TEXT UNIQUE NOT NULL, "
                    + "role TEXT NOT NULL, password_hash TEXT NOT NULL, salt BLOB NOT NULL)");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to create user schema", e);
        }
    }

    public void ensureDemoUsers() {
        List<AppUser> demos = List.of(
                AppUser.withPassword("ADMIN", "Administrator", "Admin1234"),
                AppUser.withPassword("DOCTOR", "Doctor", "Doctor1234"),
                AppUser.withPassword("NURSE", "Nurse", "Nurse1234")
        );
        for (AppUser demo : demos) {
            if (findByUsername(demo.getUsername()).isEmpty()) {
                save(demo);
            }
        }
    }

    public Optional<AppUser> findByUsername(String username) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM app_user WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to read user", e);
        }
        return Optional.empty();
    }

    public List<AppUser> findAll() {
        List<AppUser> users = new ArrayList<>();
        try (Connection connection = connect(); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT * FROM app_user")) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to list users", e);
        }
        return users;
    }

    public void save(AppUser user) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO app_user (id, username, role, password_hash, salt) VALUES (?, ?, ?, ?, ?) "
                             + "ON CONFLICT(username) DO UPDATE SET role = excluded.role, password_hash = excluded.password_hash, salt = excluded.salt")) {
            statement.setString(1, user.getId().toString());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getRole());
            statement.setString(4, user.getPasswordHash());
            statement.setBytes(5, user.getSalt());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save user", e);
        }
    }

    private AppUser mapRow(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        String username = resultSet.getString("username");
        String role = resultSet.getString("role");
        String passwordHash = resultSet.getString("password_hash");
        byte[] salt = resultSet.getBytes("salt");
        return AppUser.fromStorage(id, username, role, passwordHash, salt);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}
