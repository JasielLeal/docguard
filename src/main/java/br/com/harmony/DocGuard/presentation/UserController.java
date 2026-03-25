package br.com.harmony.DocGuard.presentation;

import br.com.harmony.DocGuard.application.services.user.CreateUser.CreateUserRequest;
import br.com.harmony.DocGuard.application.services.user.CreateUser.CreateUserService;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final CreateUserService createUserService;

    public UserController(CreateUserService createUserService ) {
        this.createUserService = createUserService;
    }

    @PostMapping
    public ApiResponse<Void> createUser(@RequestBody @Valid CreateUserRequest request) {
        return createUserService.execute(request);
    }





}
