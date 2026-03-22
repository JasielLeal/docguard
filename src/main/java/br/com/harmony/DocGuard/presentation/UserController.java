package br.com.harmony.DocGuard.presentation;


import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationRequest;
import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationResponse;
import br.com.harmony.DocGuard.application.services.auth.authentication.AuthenticationService;
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
    private final AuthenticationService authenticationService;

    public UserController(CreateUserService createUserService, AuthenticationService authenticationService) {
        this.createUserService = createUserService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createUser(@RequestBody @Valid CreateUserRequest request) {
        return createUserService.execute(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/auth")
    public ApiResponse<AuthenticationResponse> auth(@RequestBody @Valid AuthenticationRequest request, BindingResult result) {
       return authenticationService.execute(request);
    }
}
