package br.com.harmony.DocGuard.presentation;

import br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments.ListEnterpriseDocumentsResponse;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments.ListEnterpriseDocumentsService;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments.ListEnterpriseDocumentsServiceRequest;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.create.CreateEnterpriseDocumentRequest;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.create.CreateEnterpriseDocumentService;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.config.FileValidator;
import br.com.harmony.DocGuard.infrastructure.s3.S3Service;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/enterprise-documents")
public class EnterpriseDocumentController {

    private final CreateEnterpriseDocumentService createService;
    private final S3Service s3Service;
    private final ListEnterpriseDocumentsService listEnterpriseDocumentsService;
    private final FileValidator fileValidator;

    public EnterpriseDocumentController(CreateEnterpriseDocumentService createService,
                                        S3Service s3Service,
                                        ListEnterpriseDocumentsService listEnterpriseDocumentsService,
                                        FileValidator fileValidator) {
        this.createService = createService;
        this.s3Service = s3Service;
        this.listEnterpriseDocumentsService = listEnterpriseDocumentsService;
        this.fileValidator = fileValidator;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> create(
            @RequestPart("data") @Valid CreateEnterpriseDocumentRequest request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        fileValidator.validate(file);
        return ResponseEntity.ok(createService.execute(request, file, authenticatedUser));
    }

    @GetMapping("/list")
    public ApiResponse<ListEnterpriseDocumentsResponse> list(
            ListEnterpriseDocumentsServiceRequest request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return listEnterpriseDocumentsService.execute(request, authenticatedUser);
    }


}
