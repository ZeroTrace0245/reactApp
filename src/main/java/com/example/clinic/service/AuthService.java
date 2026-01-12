package com.example.clinic.service;

import com.example.clinic.data.UserRepository;
import com.example.clinic.model.AppUser;

import java.util.Optional;

public class AuthService {
    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<AppUser> authenticate(String username, String password) {
        return repository.findByUsername(username)
                .filter(user -> user.matches(password));
    }
}
