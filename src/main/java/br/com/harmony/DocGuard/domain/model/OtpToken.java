package br.com.harmony.DocGuard.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private Type type;

    private boolean used;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum Type{
        EMAIL_VERIFICATION,
        PASSWORD_RESET
    }
}


