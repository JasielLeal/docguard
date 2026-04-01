package br.com.harmony.DocGuard.presentation;

import br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments.ListEnterpriseDocumentsResponse;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments.ListEnterpriseDocumentsService;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments.ListEnterpriseDocumentsServiceRequest;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.create.CreateEnterpriseDocumentRequest;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.create.CreateEnterpriseDocumentService;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.delete.DeleteEnterpriseDocumentRequest;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.delete.DeleteEnterpriseDocumentService;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.update.UpdateEnterpriseDocumentRequest;
import br.com.harmony.DocGuard.application.services.enterpiseDocuments.update.UpdateEnterpriseDocumentService;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.config.FileValidator;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/enterprise-documents")
public class EnterpriseDocumentController {

    private final CreateEnterpriseDocumentService createService;
    private final ListEnterpriseDocumentsService listEnterpriseDocumentsService;
    private final FileValidator fileValidator;
    private final DeleteEnterpriseDocumentService deleteEnterpriseDocumentService;
    private final UpdateEnterpriseDocumentService UpdateEnterpriseDocumentService;

    public EnterpriseDocumentController(CreateEnterpriseDocumentService createService,
                                        ListEnterpriseDocumentsService listEnterpriseDocumentsService,
                                        FileValidator fileValidator,
                                        DeleteEnterpriseDocumentService deleteEnterpriseDocumentService,
                                        UpdateEnterpriseDocumentService UpdateEnterpriseDocumentService
    ) {
        this.createService = createService;
        this.listEnterpriseDocumentsService = listEnterpriseDocumentsService;
        this.fileValidator = fileValidator;
        this.deleteEnterpriseDocumentService = deleteEnterpriseDocumentService;
        this.UpdateEnterpriseDocumentService = UpdateEnterpriseDocumentService;
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

    @DeleteMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody DeleteEnterpriseDocumentRequest request,
                                    Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        if (user == null) {
            return new ApiResponse<>(false, "User not authenticated", null);
        }

        return deleteEnterpriseDocumentService.execute(request, user);
    }

    @PutMapping(path = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> update(@RequestPart("data") @Valid UpdateEnterpriseDocumentRequest request,
                                    @RequestPart("file") MultipartFile file, Authentication authentication) {
        var user = (User) authentication.getPrincipal();

        if (user == null) {
            return new ApiResponse<>(false, "User not authenticated", null);
        }

        fileValidator.validate(file);

        return UpdateEnterpriseDocumentService.execute(request, file, user);
    }


}
