package com.reactcode.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AppUser() {
    }

    public AppUser(UUID id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
