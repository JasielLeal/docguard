package br.com.harmony.DocGuard.services.auth;

import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationRequest;
import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationService;
import br.com.harmony.DocGuard.application.services.auth.jwt.JwtService;
import br.com.harmony.DocGuard.application.services.auth.session.SessionService;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private JwtService jwtService;
    @Mock private SessionService sessionService;
    @Mock private PasswordEncoder passwordEncoder;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        // Injetamos manualmente para poder passar o passwordEncoder mockado
        service = new AuthenticationService(userRepository, jwtService, sessionService, sessionRepository, passwordEncoder);
    }

    // ✅ Login com sucesso
    @Test
    void shouldAuthenticateSuccessfully() {
        var user = buildUser(User.Status.ACTIVE);
        var request = new AuthenticationRequest("user@email.com", "senha123");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("access-token");
        when(sessionService.createSession(user)).thenReturn("refresh-token");

        var response = service.execute(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getAccessToken()).isEqualTo("access-token");
        assertThat(response.getData().getRefreshToken()).isEqualTo("refresh-token");
        verify(sessionRepository).deleteAllByUser_Id(user.getId());
    }

    // ❌ E-mail não encontrado
    @Test
    void shouldThrowWhenEmailNotFound() {
        var request = new AuthenticationRequest("naoexiste@email.com", "senha123");

        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // ❌ Senha incorreta
    @Test
    void shouldThrowWhenPasswordIsWrong() {
        var user = buildUser(User.Status.ACTIVE);
        var request = new AuthenticationRequest("user@email.com", "senhaErrada");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaErrada", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid email or password");
    }

    // ❌ Conta pendente de verificação
    @Test
    void shouldThrowWhenAccountIsPendingVerification() {
        var user = buildUser(User.Status.PENDING_VERIFICATION);
        var request = new AuthenticationRequest("user@email.com", "senha123");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("pending verification");
    }

    // ✅ Sessão anterior é removida no login
    @Test
    void shouldDeletePreviousSessionOnLogin() {
        var user = buildUser(User.Status.ACTIVE);
        var request = new AuthenticationRequest("user@email.com", "senha123");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(), any(), any())).thenReturn("access-token");
        when(sessionService.createSession(user)).thenReturn("refresh-token");

        service.execute(request);

        verify(sessionRepository, times(1)).deleteAllByUser_Id(user.getId());
    }

    // Helper para criar usuário de teste
    private User buildUser(User.Status status) {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@email.com");
        user.setPassword("hashed-password");
        user.setFirstName("João");
        user.setLastName("Silva");
        user.setRole(User.Role.MEMBER);
        user.setPlan(User.Plan.FREE);
        user.setStatus(status);
        return user;
    }
}
