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
        var user =  userRepository.findByEmail(request.getEmail());

        if(user.isEmpty()){
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }

        String otp = UUID.randomUUID().toString();

        OtpToken otpToken = new OtpToken();
        otpToken.setCode(otp);
        otpToken.setUser(user.get());
        otpToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(24));
        otpToken.setUsed(false);
        otpToken.setType(OtpToken.Type.PASSWORD_RESET);

        otpRepository.save(otpToken);

        emailService.sendEmail(user.get().getEmail(), user.get().getFirstName(), "https://www.docguard.com.br/sign-up/verification?token=" + otp, EmailService.EmailType.PASSWORD_RESET);

        return new ApiResponse<>(true, "User created successfully", null);
    }
}
