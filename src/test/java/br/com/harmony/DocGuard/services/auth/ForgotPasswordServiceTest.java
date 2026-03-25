package br.com.harmony.DocGuard.services.auth;

import br.com.harmony.DocGuard.application.services.auth.forgotPassword.ForgotPasswordRequest;
import br.com.harmony.DocGuard.application.services.auth.forgotPassword.ForgotPasswordService;
import br.com.harmony.DocGuard.domain.model.OtpToken;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.email.EmailService;
import br.com.harmony.DocGuard.infrastructure.repository.optToken.OtpRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private OtpRepository otpRepository;

    private ForgotPasswordService service;

    @BeforeEach
    void setUp() {
        service = new ForgotPasswordService(userRepository, emailService, otpRepository);
    }

    // ✅ E-mail existente — deve salvar OTP e enviar e-mail
    @Test
    void shouldSaveOtpAndSendEmailWhenUserExists() {
        var user = buildUser();
        var request = new ForgotPasswordRequest();
        request.setEmail("user@email.com");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        var response = service.execute(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("If this email exists, you will receive an email");

        // verifica que o OTP foi salvo
        var otpCaptor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpRepository).save(otpCaptor.capture());
        var savedOtp = otpCaptor.getValue();
        assertThat(savedOtp.getUser()).isEqualTo(user);
        assertThat(savedOtp.getType()).isEqualTo(OtpToken.Type.PASSWORD_RESET);
        assertThat(savedOtp.isUsed()).isFalse();
        assertThat(savedOtp.getCode()).isNotBlank();

        // verifica que o e-mail foi enviado
        verify(emailService).sendEmail(
                eq(user.getEmail()),
                eq(user.getFirstName()),
                contains(savedOtp.getCode()),
                eq(EmailService.EmailType.PASSWORD_RESET)
        );
    }

    // ✅ E-mail não existente — retorna sucesso sem enviar e-mail (proteção contra enumeração)
    @Test
    void shouldReturnSuccessWithoutSendingEmailWhenUserNotFound() {
        var request = new ForgotPasswordRequest();
        request.setEmail("naoexiste@email.com");

        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        var response = service.execute(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("If this email exists, you will receive an email");

        // garante que nada foi salvo nem enviado
        verifyNoInteractions(otpRepository);
        verifyNoInteractions(emailService);
    }

    // ✅ OTP gerado deve ter expiração de 24h
    @Test
    void shouldSetOtpExpirationTo24Hours() {
        var user = buildUser();
        var request = new ForgotPasswordRequest();
        request.setEmail("user@email.com");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));

        service.execute(request);

        var otpCaptor = ArgumentCaptor.forClass(OtpToken.class);
        verify(otpRepository).save(otpCaptor.capture());

        var expiresAt = otpCaptor.getValue().getExpiresAt();
        assertThat(expiresAt).isAfter(java.time.LocalDateTime.now().plusHours(23));
        assertThat(expiresAt).isBefore(java.time.LocalDateTime.now().plusHours(25));
    }

    // ✅ Mesma resposta para e-mail existente e não existente (não vaza informação)
    @Test
    void shouldReturnSameMessageRegardlessOfEmailExistence() {
        var user = buildUser();
        var requestExistente = new ForgotPasswordRequest();
        requestExistente.setEmail("user@email.com");

        var requestInexistente = new ForgotPasswordRequest();
        requestInexistente.setEmail("naoexiste@email.com");

        when(userRepository.findByEmail("user@email.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

        var responseExistente = service.execute(requestExistente);
        var responseInexistente = service.execute(requestInexistente);

        assertThat(responseExistente.getMessage()).isEqualTo(responseInexistente.getMessage());
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
