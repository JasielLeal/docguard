package br.com.harmony.DocGuard.application.services.enterpiseDocuments.create;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class CreateEnterpriseDocumentRequest {

    @NotBlank(message = "Nome legal é obrigatório")
    private String legalName;

    @NotBlank(message = "Tipo é obrigatório")
    private String type;

    @NotNull(message = "Data de validade é obrigatória")
    @Future(message = "Data de validade deve ser futura")
    private LocalDateTime regularDate;

    @Setter
    private String documentLink;
}
