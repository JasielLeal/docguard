package br.com.harmony.DocGuard.application.services.auth.verification;

import br.com.harmony.DocGuard.domain.model.OtpToken;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.optToken.OtpRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;

    public VerificationService(OtpRepository otpRepository,  UserRepository userRepository) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApiResponse<Void> execute(VerificationRequest request) {

        var otpToken = otpRepository.findOtpTokenByCode(request.getOtpcode())
                .orElseThrow(() -> new ApiException("Invalid token", HttpStatus.BAD_REQUEST));

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException("Token expired", HttpStatus.BAD_REQUEST);
        }

        if (otpToken.isUsed()) {
            throw new ApiException("Token already used", HttpStatus.BAD_REQUEST);
        }

        if (otpToken.getType() == OtpToken.Type.EMAIL_VERIFICATION) {

            var user = otpToken.getUser();
            user.setStatus(User.Status.ACTIVE);
            userRepository.save(user);

            otpToken.setUsed(true);
            otpRepository.save(otpToken);

            return new ApiResponse<>(true, "Email verified successfully", null);
        }

        if (otpToken.getType() == OtpToken.Type.PASSWORD_RESET) {

            otpToken.setUsed(true);
            otpRepository.save(otpToken);

            return new ApiResponse<>(true, "Token valid. You can change your password.", null);
        }

        throw new ApiException("Invalid token type", HttpStatus.BAD_REQUEST);
    }
}
