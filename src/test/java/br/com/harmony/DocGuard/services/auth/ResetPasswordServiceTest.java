package br.com.harmony.DocGuard.services.auth;

import br.com.harmony.DocGuard.application.services.auth.resetPassword.ResetPasswordRequest;
import br.com.harmony.DocGuard.application.services.auth.resetPassword.ResetPasswordService;
import br.com.harmony.DocGuard.domain.model.OtpToken;
import br.com.harmony.DocGuard.domain.model.Session;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.email.EmailService;
import br.com.harmony.DocGuard.infrastructure.repository.optToken.OtpRepository;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private OtpRepository otpRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private ResetPasswordService service;

    @BeforeEach
    void setUp() {
        service = new ResetPasswordService(userRepository, emailService, otpRepository, sessionRepository, passwordEncoder);
    }

    // ✅ Reset com sucesso
    @Test
    void shouldResetPasswordSuccessfully() {
        var user = buildUser();
        var otp = buildOtp(user, false, LocalDateTime.now().plusHours(1), OtpToken.Type.PASSWORD_RESET);
        var session = new Session();
        var request = buildRequest("valid-otp", user.getEmail(), "novaSenha123");

        when(otpRepository.findOtpTokenByCode("valid-otp")).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hashed-password");
        when(sessionRepository.findByUser_Id(user.getId())).thenReturn(Optional.of(session));

        var response = service.execute(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Password reset successfully");
        assertThat(user.getPassword()).isEqualTo("hashed-password");
        assertThat(otp.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(otpRepository).save(otp);
        verify(sessionRepository).delete(session);
        verify(emailService).sendEmail(eq(user.getEmail()), eq(user.getFirstName()), isNull(), eq(EmailService.EmailType.PASSWORD_CHANGED));
    }

    // ❌ Token não encontrado
    @Test
    void shouldThrowWhenTokenNotFound() {
        var request = buildRequest("token-invalido", "user@email.com", "novaSenha123");

        when(otpRepository.findOtpTokenByCode("token-invalido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid token");
    }

    // ❌ Token já usado
    @Test
    void shouldThrowWhenTokenAlreadyUsed() {
        var user = buildUser();
        var otp = buildOtp(user, true, LocalDateTime.now().plusHours(1), OtpToken.Type.PASSWORD_RESET);
        var request = buildRequest("used-otp", user.getEmail(), "novaSenha123");

        when(otpRepository.findOtpTokenByCode("used-otp")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Token already used");
    }

    // ❌ E-mail não bate com o token
    @Test
    void shouldThrowWhenEmailDoesNotMatchToken() {
        var user = buildUser();
        var otp = buildOtp(user, false, LocalDateTime.now().plusHours(1), OtpToken.Type.PASSWORD_RESET);
        var request = buildRequest("valid-otp", "outro@email.com", "novaSenha123");

        when(otpRepository.findOtpTokenByCode("valid-otp")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid token");
    }

    // ❌ Token expirado
    @Test
    void shouldThrowWhenTokenIsExpired() {
        var user = buildUser();
        var otp = buildOtp(user, false, LocalDateTime.now().minusHours(1), OtpToken.Type.PASSWORD_RESET);
        var request = buildRequest("expired-otp", user.getEmail(), "novaSenha123");

        when(otpRepository.findOtpTokenByCode("expired-otp")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Token expired");
    }

    // ❌ Tipo do token incorreto
    @Test
    void shouldThrowWhenTokenTypeIsWrong() {
        var user = buildUser();
        var otp = buildOtp(user, false, LocalDateTime.now().plusHours(1), OtpToken.Type.EMAIL_VERIFICATION);
        var request = buildRequest("wrong-type-otp", user.getEmail(), "novaSenha123");

        when(otpRepository.findOtpTokenByCode("wrong-type-otp")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid token type");
    }

    // ✅ Sem sessão ativa — não deve lançar exceção
    @Test
    void shouldNotThrowWhenNoActiveSession() {
        var user = buildUser();
        var otp = buildOtp(user, false, LocalDateTime.now().plusHours(1), OtpToken.Type.PASSWORD_RESET);
        var request = buildRequest("valid-otp", user.getEmail(), "novaSenha123");

        when(otpRepository.findOtpTokenByCode("valid-otp")).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hashed-password");
        when(sessionRepository.findByUser_Id(user.getId())).thenReturn(Optional.empty());

        assertThatCode(() -> service.execute(request)).doesNotThrowAnyException();
        verify(sessionRepository, never()).delete(any());
    }

    // ✅ E-mail de confirmação deve ser enviado após reset
    @Test
    void shouldSendConfirmationEmailAfterReset() {
        var user = buildUser();
        var otp = buildOtp(user, false, LocalDateTime.now().plusHours(1), OtpToken.Type.PASSWORD_RESET);
        var request = buildRequest("valid-otp", user.getEmail(), "novaSenha123");

        when(otpRepository.findOtpTokenByCode("valid-otp")).thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hashed-password");
        when(sessionRepository.findByUser_Id(user.getId())).thenReturn(Optional.empty());

        service.execute(request);

        verify(emailService, times(1)).sendEmail(
                eq(user.getEmail()),
                eq(user.getFirstName()),
                isNull(),
                eq(EmailService.EmailType.PASSWORD_CHANGED)
        );
    }

    private User buildUser() {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@email.com");
        user.setFirstName("João");
        user.setLastName("Silva");
        user.setPassword("old-hashed-password");
        user.setRole(User.Role.MEMBER);
        user.setPlan(User.Plan.FREE);
        user.setStatus(User.Status.ACTIVE);
        return user;
    }

    private OtpToken buildOtp(User user, boolean used, LocalDateTime expiresAt, OtpToken.Type type) {
        var otp = new OtpToken();
        otp.setCode(UUID.randomUUID().toString());
        otp.setUser(user);
        otp.setUsed(used);
        otp.setExpiresAt(expiresAt);
        otp.setType(type);
        return otp;
    }

    private ResetPasswordRequest buildRequest(String otpCode, String email, String newPassword) {
        var request = new ResetPasswordRequest();
        request.setOtpCode(otpCode);
        request.setEmail(email);
        request.setNewPassword(newPassword);
        return request;
    }
}
