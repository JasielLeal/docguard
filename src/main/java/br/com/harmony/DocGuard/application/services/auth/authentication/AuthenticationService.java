package br.com.harmony.DocGuard.application.services.auth.authentication;

import br.com.harmony.DocGuard.application.services.auth.jwt.JwtService;
import br.com.harmony.DocGuard.application.services.auth.session.SessionService;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public AuthenticationService(UserRepository repository,
                                 JwtService jwtService,
                                 SessionService sessionService,
                                 SessionRepository sessionRepository,
                                    PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public ApiResponse<AuthenticationResponse> execute(AuthenticationRequest request) {
        var userOpt = repository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            // proteção contra timing attack (boa prática)
            passwordEncoder.matches(request.getPassword(), "$2a$10$abcdefghijklmnopqrstuv");
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        var user = userOpt.get();

        // ✅ Verifique o status antes de validar a senha
        if (user.getStatus() == User.Status.PENDING_VERIFICATION) {
            throw new ApiException("User account pending verification", HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        sessionRepository.deleteAllByUser_Id(user.getId());

        String accessToken = jwtService.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                user.getPlan().name()
        );

        String refreshToken = sessionService.createSession(user);

        AuthenticationResponse response = new AuthenticationResponse(accessToken, refreshToken);

        return new ApiResponse<>(true, "Authenticated successfully", response);
    }
}
