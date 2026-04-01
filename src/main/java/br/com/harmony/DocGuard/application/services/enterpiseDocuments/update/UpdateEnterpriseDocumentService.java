package br.com.harmony.DocGuard.application.services.enterpiseDocuments.update;


import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.enterprise.EnterpriseRepository;
import br.com.harmony.DocGuard.infrastructure.repository.enterpriseDocuments.EnterpriseDocumentsRepository;
import br.com.harmony.DocGuard.infrastructure.s3.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateEnterpriseDocumentService {

    private final S3Service s3Service;
    private final EnterpriseDocumentsRepository enterpriseDocumentsRepository;
    private final EnterpriseRepository enterpriseRepository;

    public UpdateEnterpriseDocumentService(S3Service s3Service, EnterpriseDocumentsRepository enterpriseDocumentsRepository, EnterpriseRepository enterpriseRepository) {
        this.s3Service = s3Service;
        this.enterpriseDocumentsRepository = enterpriseDocumentsRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    @Transactional
    public ApiResponse<Void> execute(UpdateEnterpriseDocumentRequest request, MultipartFile file, User authenticatedUser) {

        var enterprise = enterpriseRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new ApiException("Enterprise not found", HttpStatus.NOT_FOUND));

        var document = enterpriseDocumentsRepository.findById(UUID.fromString(request.getDocumentId()))
                .orElseThrow(() -> new ApiException("Document not found", HttpStatus.NOT_FOUND));

        if (!document.getEnterprise().getId().equals(enterprise.getId())) {
            throw new ApiException("Document does not belong to the authenticated user's enterprise", HttpStatus.FORBIDDEN);
        }

        String documentLink = s3Service.upload(file, "enterprise-documents", authenticatedUser.getId(), document.getLegalName());

        document.setRegularDate(request.getRegularDate());
        document.setDocumentLink(documentLink);
        document.setCreatedAt(LocalDateTime.now());

        return new ApiResponse<>(true, "Document updated successfully", null);
    }
}
