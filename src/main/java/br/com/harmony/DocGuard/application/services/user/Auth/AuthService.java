package br.com.harmony.DocGuard.application.services.user.Auth;

import br.com.harmony.DocGuard.infrastructure.repository.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public AuthService(UserRepository repository, JwtService jwtService,  SessionService sessionService) {
        this.repository = repository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    public ApiResponse<AuthResponse> execute(AuthRequest request) {

        var userOpt = repository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            // proteção contra timing attack (boa prática)
            passwordEncoder.matches(request.getPassword(), "$2a$10$abcdefghijklmnopqrstuv");
            return new ApiResponse<>(false, "Invalid email or password", null);
        }

        var user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new ApiResponse<>(false, "Invalid email or password", null);
        }

        String accessToken = jwtService.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                user.getPlan().name()
        );

        String refreshToken = sessionService.createSession(user);

        AuthResponse response = new AuthResponse(accessToken, refreshToken);

        return new ApiResponse<>(true, "Authenticated successfully", response);
    }
}
