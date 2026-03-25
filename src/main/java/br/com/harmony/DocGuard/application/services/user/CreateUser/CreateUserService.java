package br.com.harmony.DocGuard.application.services.user.CreateUser;

import br.com.harmony.DocGuard.domain.model.OtpToken;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.email.EmailService;
import br.com.harmony.DocGuard.infrastructure.repository.optToken.OtpRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateUserService {

    private final UserRepository repository;
    private final EmailService emailService;
    private final OtpRepository otpRepository;

    public CreateUserService(UserRepository repository, EmailService emailService,  OtpRepository otpRepository) {
        this.emailService = emailService;
        this.repository = repository;
        this.otpRepository = otpRepository;
    }

    @Transactional
    public ApiResponse<Void> execute(CreateUserRequest request) {

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("Email already exists", HttpStatus.BAD_REQUEST);
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(User.Role.MEMBER);
        user.setPlan(User.Plan.FREE);
        user.setStatus(User.Status.PENDING_VERIFICATION);

        repository.save(user);

        String token = UUID.randomUUID().toString();

        OtpToken otpToken = new OtpToken();
        otpToken.setCode(token);
        otpToken.setUser(user);
        otpToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(24));
        otpToken.setUsed(false);
        otpToken.setType(OtpToken.Type.EMAIL_VERIFICATION);

        otpRepository.save(otpToken);

        emailService.sendEmail(user.getEmail(), user.getFirstName(), "https://www.docguard.com.br/sign-up/verify-email?token" + token, EmailService.EmailType.EMAIL_VERIFICATION);

        return new ApiResponse<>(true, "User created successfully", null);
    }
}
