package br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListEnterpriseDocumentsServiceRequest {

    private String query;   // busca por nome

    private Integer take = 10;  // limite

    private Integer skip = 0;   // offset

    private String status;  // Válido, Atenção, Crítico, Vencido
}
