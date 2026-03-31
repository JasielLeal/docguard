package br.com.harmony.DocGuard.application.services.enterpiseDocuments.create;


import br.com.harmony.DocGuard.domain.model.EnterpriseDocuments;
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

@Service
public class CreateEnterpriseDocumentService {

    private final EnterpriseDocumentsRepository enterpriseDocumentsRepository;
    private final EnterpriseRepository enterpriseRepository;
    private final S3Service s3Service;

    public CreateEnterpriseDocumentService(EnterpriseDocumentsRepository enterpriseDocumentsRepository, EnterpriseRepository enterpriseRepository, S3Service s3Service) {
        this.enterpriseDocumentsRepository = enterpriseDocumentsRepository;
        this.enterpriseRepository = enterpriseRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public ApiResponse<Void> execute(CreateEnterpriseDocumentRequest request, MultipartFile file, User authenticatedUser) {
        var enterprise = enterpriseRepository.findByUserId(authenticatedUser.getId())
                .orElseThrow(() -> new ApiException("Enterprise not found", HttpStatus.NOT_FOUND));

        String documentLink = s3Service.upload(file, "enterprise-documents", authenticatedUser.getId(), request.getLegalName());

        try {
            EnterpriseDocuments document = EnterpriseDocuments.builder()
                    .legalName(request.getLegalName())
                    .type(request.getType())
                    .regularDate(request.getRegularDate())
                    .documentLink(documentLink)
                    .createdAt(LocalDateTime.now())
                    .enterprise(enterprise)
                    .build();

            enterpriseDocumentsRepository.save(document);
        } catch (Exception e) {
            throw new ApiException("Erro ao salvar documento", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ApiResponse<>(true, "Document saved successfully", null);
    }
}
