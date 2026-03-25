package br.com.harmony.DocGuard.infrastructure.repository.session;

import br.com.harmony.DocGuard.domain.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByRefreshToken(String refreshToken);
    Optional<Session> findByUserId(UUID userId);
    Optional<Session> removeSessionByUser_Id(UUID userId);

    Optional<Session> findByUser_Id(UUID userId);

    void deleteAllByUser_Id(UUID id);
}
