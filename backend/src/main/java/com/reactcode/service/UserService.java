package com.reactcode.service;

import com.reactcode.api.BadRequestException;
import com.reactcode.domain.AppUser;
import com.reactcode.repo.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;

    public UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AppUser resolveOrCreate(String rawUserId) {
        UUID id;
        if (rawUserId == null || rawUserId.isBlank()) {
            id = UUID.randomUUID();
        } else {
            try {
                id = UUID.fromString(rawUserId.trim());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("X-User-Id must be a UUID");
            }
        }

        return appUserRepository.findById(id)
                .orElseGet(() -> appUserRepository.save(new AppUser(id, Instant.now())));
    }
}
