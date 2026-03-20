package br.com.harmony.DocGuard.application.services.user.CreateUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters")
    private String password;
}
