package com.ealth.codeleat.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;  // Unique DB identifier

    @Column(nullable = false)
    private String token;  // The actual JWT string or JTI

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Linked user

    @Column(nullable = false)
    private Instant expiryDate; // Expiration time

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    //Constructors
    public RefreshToken() {}

    public RefreshToken(String token, User user, Instant expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}

