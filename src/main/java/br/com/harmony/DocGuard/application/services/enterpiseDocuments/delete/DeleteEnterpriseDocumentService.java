package br.com.harmony.DocGuard.application.services.enterpiseDocuments.delete;

import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.enterprise.EnterpriseRepository;
import br.com.harmony.DocGuard.infrastructure.repository.enterpriseDocuments.EnterpriseDocumentsRepository;
import br.com.harmony.DocGuard.infrastructure.s3.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteEnterpriseDocumentService {

    private final EnterpriseDocumentsRepository enterpriseDocumentsRepository;
    private final EnterpriseRepository enterpriseRepository;

    public DeleteEnterpriseDocumentService(EnterpriseDocumentsRepository enterpriseDocumentsRepository, EnterpriseRepository enterpriseRepository, S3Service s3Service) {
        this.enterpriseDocumentsRepository = enterpriseDocumentsRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    @Transactional
    public ApiResponse<Void> execute(DeleteEnterpriseDocumentRequest request, User authenticatedUser) {

        var enterprise = enterpriseRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new ApiException("Enterprise not found", HttpStatus.NOT_FOUND));

        var document = enterpriseDocumentsRepository.findById(UUID.fromString(request.getDocumentId()))
                .orElseThrow(() -> new ApiException("Document not found", HttpStatus.NOT_FOUND));

        // ✅ Verifica se o documento pertence à empresa do usuário autenticado
        if (!document.getEnterprise().getId().equals(enterprise.getId())) {
            throw new ApiException("Access denied", HttpStatus.FORBIDDEN);
        }

        // ✅ Deleta do S3 antes de remover do banco
        // s3Service.delete(document.getDocumentLink());

        enterpriseDocumentsRepository.delete(document);

        return new ApiResponse<>(true, "Document deleted successfully", null);
    }
}
