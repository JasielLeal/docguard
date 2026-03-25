package br.com.harmony.DocGuard.application.services.auth.verification;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequest {

    @NotBlank
    private String otpcode;
}
