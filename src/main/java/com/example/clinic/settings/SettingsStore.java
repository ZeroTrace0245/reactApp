package com.example.clinic.settings;

import com.example.clinic.model.AppUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

public class SettingsStore {
    private static final Path STORE_PATH = Paths.get("config", "settings.json");

    private final ObjectMapper mapper;
    private SettingsData data;

    public SettingsStore() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        load();
    }

    private void load() {
        try {
            if (Files.exists(STORE_PATH)) {
                data = mapper.readValue(STORE_PATH.toFile(), SettingsData.class);
            } else {
                data = new SettingsData();
            }
        } catch (IOException e) {
            data = new SettingsData();
        }
    }

    public Optional<String> getLastUsername() {
        return Optional.ofNullable(data.lastUsername);
    }

    public Optional<String> getLastRole() {
        return Optional.ofNullable(data.lastRole);
    }

    public void persistLastLogin(AppUser user) {
        data.lastUsername = user.getUsername();
        data.lastRole = user.getRole();
        data.lastLogin = Instant.now().toString();
        save();
    }

    public void clear() {
        data = new SettingsData();
        save();
    }

    private void save() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            mapper.writeValue(STORE_PATH.toFile(), data);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to persist settings", e);
        }
    }

    private static final class SettingsData {
        public String lastUsername;
        public String lastRole;
        public String lastLogin;
    }
}
