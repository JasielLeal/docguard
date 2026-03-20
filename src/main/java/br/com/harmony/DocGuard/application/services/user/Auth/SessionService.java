package br.com.harmony.DocGuard.application.services.user.Auth;

import br.com.harmony.DocGuard.domain.model.Session;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository repository;

    public SessionService(SessionRepository repository) {
        this.repository = repository;
    }

    public String createSession(User user) {
        String refreshToken = UUID.randomUUID().toString();

        Session session = new Session();
        session.setUser(user);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        session.setRevoked(false);

        repository.save(session);

        return refreshToken;
    }
}