package br.com.harmony.DocGuard.presentation;

import br.com.harmony.DocGuard.application.services.user.CreateUser.CreateUserRequest;
import br.com.harmony.DocGuard.application.services.user.CreateUser.CreateUserService;
import br.com.harmony.DocGuard.application.services.user.Me.MeResponse;
import br.com.harmony.DocGuard.application.services.user.Me.MeService;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final CreateUserService createUserService;
    private final MeService meService;

    public UserController(CreateUserService createUserService, MeService meService) {
        this.createUserService = createUserService;
        this.meService = meService;
    }

    @PostMapping("/create")
    public ApiResponse<Void> createUser(@RequestBody @Valid CreateUserRequest request) {
        return createUserService.execute(request);
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return meService.execute(user);
    }





}
