package br.com.harmony.DocGuard.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    String refreshToken;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    Instant expiresAt;
    boolean revoked;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isRevoked() {
        return revoked;
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}

