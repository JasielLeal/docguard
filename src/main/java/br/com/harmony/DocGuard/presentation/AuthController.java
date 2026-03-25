package br.com.harmony.DocGuard.presentation;

import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationRequest;
import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationResponse;
import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationService;
import br.com.harmony.DocGuard.application.services.auth.forgotPassword.ForgotPasswordRequest;
import br.com.harmony.DocGuard.application.services.auth.forgotPassword.ForgotPasswordService;
import br.com.harmony.DocGuard.application.services.auth.refreshToken.RefreshTokenRequest;
import br.com.harmony.DocGuard.application.services.auth.refreshToken.RefreshTokenResponse;
import br.com.harmony.DocGuard.application.services.auth.refreshToken.RefreshTokenService;
import br.com.harmony.DocGuard.application.services.auth.resetPassword.ResetPasswordRequest;
import br.com.harmony.DocGuard.application.services.auth.resetPassword.ResetPasswordService;
import br.com.harmony.DocGuard.application.services.auth.verification.VerificationRequest;
import br.com.harmony.DocGuard.application.services.auth.verification.VerificationService;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final VerificationService verificationService;
    private final AuthenticationService authenticationService;
    private final ForgotPasswordService forgotPasswordService;
    private final ResetPasswordService resetPasswordService;

    public AuthController(RefreshTokenService refreshTokenService,
                          VerificationService verificationService,
                          AuthenticationService authenticationService,
                          ForgotPasswordService forgotPasswordService,
                          ResetPasswordService resetPasswordService) {
        this.refreshTokenService = refreshTokenService;
        this.verificationService = verificationService;
        this.authenticationService = authenticationService;
        this.forgotPasswordService = forgotPasswordService;
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping()
    public ApiResponse<AuthenticationResponse> auth(@RequestBody @Valid AuthenticationRequest request) {
        return authenticationService.execute(request);
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request){
        return refreshTokenService.execute(request);
    }

    @PostMapping("/verification-otp")
    public ApiResponse<Void> verification(@RequestBody @Valid VerificationRequest request) {
        return verificationService.execute(request);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        return forgotPasswordService.execute(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request){
        return resetPasswordService.execute(request);
    }
}
