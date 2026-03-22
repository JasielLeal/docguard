package br.com.harmony.DocGuard.presentation;

import br.com.harmony.DocGuard.application.services.auth.refreshToken.RefreshTokenRequest;
import br.com.harmony.DocGuard.application.services.auth.refreshToken.RefreshTokenService;
import br.com.harmony.DocGuard.infrastructure.repository.config.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    public AuthController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> refresh(@RequestBody @Valid RefreshTokenRequest request){
        return refreshTokenService.execute(request);
    }
}
