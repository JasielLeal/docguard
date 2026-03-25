package br.com.harmony.DocGuard.application.services.auth.resetPassword;

import br.com.harmony.DocGuard.application.services.auth.session.SessionService;
import br.com.harmony.DocGuard.domain.model.OtpToken;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.email.EmailService;
import br.com.harmony.DocGuard.infrastructure.repository.optToken.OtpRepository;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetPasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRepository sessionRepository;

    public ResetPasswordService(UserRepository userRepository, EmailService emailService, OtpRepository otpRepository,
                                SessionRepository sessionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.otpRepository = otpRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ApiResponse<Void> execute(ResetPasswordRequest request){

        var otpToken = otpRepository.findOtpTokenByCode(request.getOtpCode())
                .orElseThrow(() -> new ApiException("Invalid token", HttpStatus.BAD_REQUEST));

        if (otpToken.isUsed()) {
            throw new ApiException("Token already used", HttpStatus.BAD_REQUEST);
        }

        if (!otpToken.getUser().getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new ApiException("Invalid token", HttpStatus.BAD_REQUEST);
        }

        if (otpToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ApiException("Token expired", HttpStatus.BAD_REQUEST);
        }

        if (otpToken.getType() != OtpToken.Type.PASSWORD_RESET) {
            throw new ApiException("Invalid token type", HttpStatus.BAD_REQUEST);
        }

        var user = otpToken.getUser();

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());

        user.setPassword(encodedPassword);
        userRepository.save(user);

        otpToken.setUsed(true);
        otpRepository.save(otpToken);

        sessionRepository.findByUser_Id(user.getId())
                .ifPresent(sessionRepository::delete);

        emailService.sendEmail(
                user.getEmail(),
                user.getFirstName(),
                null,
                EmailService.EmailType.PASSWORD_CHANGED
        );

        return new ApiResponse<>(true, "Password reset successfully", null);
    }


}
