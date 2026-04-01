package br.com.harmony.DocGuard.application.services.enterpiseDocuments.delete;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class DeleteEnterpriseDocumentRequest {

    @NotBlank
    @Length(max = 255)
    private String documentId;
}
