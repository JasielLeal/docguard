package br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments;

import br.com.harmony.DocGuard.domain.model.EnterpriseDocuments;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Builder
public class ListEnterpriseDocumentsResponse {

    private List<DocumentInfo> documents;
    private long total;
    private Integer take = 10;
    private Integer skip = 0;

    @Getter
    @Builder
    public static class DocumentInfo {

        private String id;
        private String name;
        private String type;
        private LocalDateTime uploadedAt;
        private LocalDateTime expiresAt;
        private String status;
        private long daysLeft;
        private String fileUrl;

        public static DocumentInfo fromEntity(EnterpriseDocuments doc) {

            long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), doc.getRegularDate());

            String status;

            if (daysLeft < 0) status = "Vencido";
            else if (daysLeft <= 7) status = "Crítico";
            else if (daysLeft <= 15) status = "Atenção";
            else status = "Válido";

            return DocumentInfo.builder()
                    .id(String.valueOf(doc.getId()))
                    .name(doc.getLegalName())
                    .type(doc.getType())
                    .uploadedAt(doc.getCreatedAt())
                    .expiresAt(doc.getRegularDate())
                    .status(status)
                    .daysLeft(daysLeft)
                    .fileUrl(doc.getDocumentLink())
                    .build();
        }
    }
}