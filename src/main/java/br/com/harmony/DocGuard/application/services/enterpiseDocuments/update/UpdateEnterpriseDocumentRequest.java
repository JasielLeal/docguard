package br.com.harmony.DocGuard.application.services.enterpiseDocuments.update;


import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateEnterpriseDocumentRequest {

    private String documentId;
    private LocalDateTime regularDate;
}
