package br.com.harmony.DocGuard.application.services.auth.authentication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AuthenticationRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters")
    private String password;
}
