package br.com.harmony.DocGuard.application.services.auth.forgotPassword;

import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationRequest;
import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationResponse;
import br.com.harmony.DocGuard.domain.model.OtpToken;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.email.EmailService;
import br.com.harmony.DocGuard.infrastructure.repository.optToken.OtpRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpRepository otpRepository;

    public ForgotPasswordService(UserRepository userRepository,  EmailService emailService,OtpRepository otpRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
    }

    @Transactional
    public ApiResponse<Void> execute(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail());

        if (user.isEmpty()) {
            return new ApiResponse<>(true, "If this email exists, you will receive an email", null);
        }

        // Invalida OTPs anteriores do mesmo tipo
        otpRepository.findAllByUserIdAndTypeAndUsedFalse(user.get().getId(), OtpToken.Type.PASSWORD_RESET)
                .forEach(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                });

        // Gera novo OTP seguro
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        String otp = java.util.HexFormat.of().formatHex(bytes);

        OtpToken otpToken = new OtpToken();
        otpToken.setCode(otp);
        otpToken.setUser(user.get());
        otpToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(24));
        otpToken.setUsed(false);
        otpToken.setType(OtpToken.Type.PASSWORD_RESET);

        otpRepository.save(otpToken);

        emailService.sendEmail(
                user.get().getEmail(),
                user.get().getFirstName(),
                "http://localhost:3000/reset-password?token=" + otp,
                EmailService.EmailType.PASSWORD_RESET
        );

        return new ApiResponse<>(true, "If this email exists, you will receive an email", null);
    }
}
