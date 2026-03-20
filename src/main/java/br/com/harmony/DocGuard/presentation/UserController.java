package br.com.harmony.DocGuard.presentation;


import br.com.harmony.DocGuard.application.services.user.Auth.AuthRequest;
import br.com.harmony.DocGuard.application.services.user.Auth.AuthResponse;
import br.com.harmony.DocGuard.application.services.user.Auth.AuthService;
import br.com.harmony.DocGuard.application.services.user.CreateUser.CreateUserRequest;
import br.com.harmony.DocGuard.application.services.user.CreateUser.CreateUserService;
import br.com.harmony.DocGuard.infrastructure.repository.config.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserService createUserService;
    private final AuthService authService;

    public UserController(CreateUserService createUserService, AuthService authService) {
        this.createUserService = createUserService;
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createUser(@RequestBody @Valid CreateUserRequest request) {
        return createUserService.execute(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/auth")
    public ApiResponse<AuthResponse> createUser(@RequestBody @Valid AuthRequest request, BindingResult result) {
       return authService.execute(request);
    }
}
