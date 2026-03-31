package br.com.harmony.DocGuard.application.services.auth.resetPassword;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    @Size(min = 6, max = 128)
    String newPassword;

    @NotBlank
    String otpCode;
}
