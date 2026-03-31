package br.com.harmony.DocGuard.application.services.enterprise.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEnterpriseRequest {

    @NotBlank
    @Size(min = 14, max = 14, message = "CNPJ must have 14 digits")
    private String cnpj;
}