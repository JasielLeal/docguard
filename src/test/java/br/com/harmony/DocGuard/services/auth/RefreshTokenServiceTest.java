package br.com.harmony.DocGuard.services.auth;

import br.com.harmony.DocGuard.application.services.auth.jwt.JwtService;
import br.com.harmony.DocGuard.application.services.auth.refreshToken.RefreshTokenRequest;
import br.com.harmony.DocGuard.application.services.auth.refreshToken.RefreshTokenService;
import br.com.harmony.DocGuard.application.services.auth.session.SessionService;
import br.com.harmony.DocGuard.domain.model.Session;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private JwtService jwtService;
    @Mock private SessionService sessionService;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(sessionRepository, jwtService, sessionService);
    }

    // ✅ Refresh token válido — deve deletar sessão atual e retornar novos tokens
    @Test
    void shouldRefreshTokensSuccessfully() {
        var user = buildUser();
        var session = buildSession(user, false, Instant.now().plusSeconds(3600));
        var request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(sessionRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(session));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("new-access-token");
        when(sessionService.createSession(user)).thenReturn("new-refresh-token");

        var response = service.execute(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getData().getRefreshToken()).isEqualTo("new-refresh-token");
        verify(sessionRepository).delete(session);
    }

    // ❌ Refresh token não encontrado
    @Test
    void shouldThrowWhenRefreshTokenNotFound() {
        var request = new RefreshTokenRequest();
        request.setRefreshToken("token-inexistente");

        when(sessionRepository.findByRefreshToken("token-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    // ❌ Refresh token expirado
    @Test
    void shouldThrowWhenRefreshTokenIsExpired() {
        var user = buildUser();
        var session = buildSession(user, false, Instant.now().minusSeconds(3600)); // expirado
        var request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        when(sessionRepository.findByRefreshToken("expired-token")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(sessionRepository, never()).delete(any());
    }

    // ❌ Refresh token revogado
    @Test
    void shouldThrowWhenRefreshTokenIsRevoked() {
        var user = buildUser();
        var session = buildSession(user, true, Instant.now().plusSeconds(3600)); // revogado
        var request = new RefreshTokenRequest();
        request.setRefreshToken("revoked-token");

        when(sessionRepository.findByRefreshToken("revoked-token")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(sessionRepository, never()).delete(any());
    }

    // ✅ Sessão antiga deve ser deletada ao fazer refresh
    @Test
    void shouldDeleteOldSessionOnRefresh() {
        var user = buildUser();
        var session = buildSession(user, false, Instant.now().plusSeconds(3600));
        var request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(sessionRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(session));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("new-access-token");
        when(sessionService.createSession(user)).thenReturn("new-refresh-token");

        service.execute(request);

        verify(sessionRepository, times(1)).delete(session);
    }

    // ✅ Nova sessão deve ser criada ao fazer refresh
    @Test
    void shouldCreateNewSessionOnRefresh() {
        var user = buildUser();
        var session = buildSession(user, false, Instant.now().plusSeconds(3600));
        var request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        when(sessionRepository.findByRefreshToken("valid-refresh-token")).thenReturn(Optional.of(session));
        when(jwtService.generateToken(any(), any(), any())).thenReturn("new-access-token");
        when(sessionService.createSession(user)).thenReturn("new-refresh-token");

        service.execute(request);

        verify(sessionService, times(1)).createSession(user);
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

    private Session buildSession(User user, boolean revoked, Instant expiresAt) {
        var session = new Session();
        session.setUser(user);
        session.setRevoked(revoked);
        session.setExpiresAt(expiresAt);
        session.setRefreshToken(UUID.randomUUID().toString());
        return session;
    }
}
