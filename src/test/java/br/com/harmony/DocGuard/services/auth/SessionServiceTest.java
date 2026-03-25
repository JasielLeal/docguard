package br.com.harmony.DocGuard.services.auth;

import br.com.harmony.DocGuard.application.services.auth.session.SessionService;
import br.com.harmony.DocGuard.domain.model.Session;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock private SessionRepository sessionRepository;

    private SessionService service;

    @BeforeEach
    void setUp() {
        service = new SessionService(sessionRepository);
    }

    // ✅ Deve retornar um refresh token não vazio
    @Test
    void shouldReturnNonEmptyRefreshToken() {
        var user = buildUser();

        String refreshToken = service.createSession(user);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotBlank();
    }

    // ✅ Cada chamada deve gerar um refresh token único
    @Test
    void shouldGenerateUniqueRefreshTokens() {
        var user = buildUser();

        String token1 = service.createSession(user);
        String token2 = service.createSession(user);

        assertThat(token1).isNotEqualTo(token2);
    }

    // ✅ Sessão salva deve pertencer ao usuário correto
    @Test
    void shouldSaveSessionWithCorrectUser() {
        var user = buildUser();

        service.createSession(user);

        var captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    // ✅ Sessão não deve estar revogada ao ser criada
    @Test
    void shouldCreateSessionAsNotRevoked() {
        var user = buildUser();

        service.createSession(user);

        var captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().isRevoked()).isFalse();
    }

    // ✅ Sessão deve expirar em 7 dias
    @Test
    void shouldSetExpirationTo7Days() {
        var user = buildUser();

        service.createSession(user);

        var captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        var expiresAt = captor.getValue().getExpiresAt();

        assertThat(expiresAt).isAfter(Instant.now().plusSeconds(6 * 24 * 3600));
        assertThat(expiresAt).isBefore(Instant.now().plusSeconds(8 * 24 * 3600));
    }

    // ✅ Refresh token salvo na sessão deve ser o mesmo retornado
    @Test
    void shouldSaveTheSameRefreshTokenThatIsReturned() {
        var user = buildUser();

        String returnedToken = service.createSession(user);

        var captor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(captor.capture());
        assertThat(captor.getValue().getRefreshToken()).isEqualTo(returnedToken);
    }

    // ✅ Repositório deve ser chamado exatamente uma vez
    @Test
    void shouldSaveSessionExactlyOnce() {
        var user = buildUser();

        service.createSession(user);

        verify(sessionRepository, times(1)).save(any(Session.class));
    }

    private User buildUser() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@email.com");
        user.setFirstName("João");
        user.setLastName("Silva");
        user.setRole(User.Role.MEMBER);
        user.setPlan(User.Plan.FREE);
        user.setStatus(User.Status.ACTIVE);
        return user;
    }
}
