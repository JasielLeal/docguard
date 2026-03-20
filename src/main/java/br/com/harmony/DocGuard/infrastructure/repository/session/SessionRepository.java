package br.com.harmony.DocGuard.infrastructure.repository.session;

import br.com.harmony.DocGuard.domain.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByRefreshToken(String refreshToken);
}
