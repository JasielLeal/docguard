package br.com.harmony.DocGuard.application.services.auth.refreshToken;

import br.com.harmony.DocGuard.application.services.auth.jwt.JwtService;
import br.com.harmony.DocGuard.application.services.auth.session.SessionService;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final SessionRepository sessionRepository;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public RefreshTokenService(SessionRepository sessionRepository,
                               JwtService jwtService,
                               SessionService sessionService) {
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Transactional
    public ApiResponse<RefreshTokenResponse> execute(RefreshTokenRequest request) {
        var session = sessionRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (session.isExpired()) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        if (session.isRevoked()) {
            throw new ApiException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }

        // revoga a sessão atual (rotation)
        sessionRepository.delete(session);

        // gera novo access token
        var user = session.getUser();
        String newAccessToken = jwtService.generateToken(
                user.getId().toString(),
                user.getRole().name(),
                user.getPlan().name()
        );

        // gera novo refresh token (rotation)
        String newRefreshToken = sessionService.createSession(user);

        return new ApiResponse<>(true, "Token refreshed", new RefreshTokenResponse(newAccessToken, newRefreshToken));
    }
}
