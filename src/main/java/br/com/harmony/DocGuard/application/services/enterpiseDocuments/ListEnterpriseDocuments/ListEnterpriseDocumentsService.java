package br.com.harmony.DocGuard.application.services.enterpiseDocuments.ListEnterpriseDocuments;


import br.com.harmony.DocGuard.domain.model.EnterpriseDocuments;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.enterprise.EnterpriseRepository;
import br.com.harmony.DocGuard.infrastructure.repository.enterpriseDocuments.EnterpriseDocumentsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ListEnterpriseDocumentsService {

    private final EnterpriseDocumentsRepository enterpriseDocumentsRepository;
    private final EnterpriseRepository enterpriseRepository;

    public ListEnterpriseDocumentsService(
            EnterpriseDocumentsRepository enterpriseDocumentsRepository,
            EnterpriseRepository enterpriseRepository
    ) {
        this.enterpriseDocumentsRepository = enterpriseDocumentsRepository;
        this.enterpriseRepository = enterpriseRepository;
    }

    public ApiResponse<ListEnterpriseDocumentsResponse> execute(
            ListEnterpriseDocumentsServiceRequest request,
            User authenticatedUser
    ) {

        var enterprise = enterpriseRepository.findByUserId(authenticatedUser.getId());

        if (enterprise.isEmpty()) {
            throw new ApiException("Enterprise not found for user", HttpStatus.BAD_REQUEST);
        }

        int take = request.getTake() != null ? request.getTake() : 5;
        int skip = request.getSkip() != null ? request.getSkip() : 0;

        if (take > 50) {
            take = 50;
        }

        int page = skip / take;

        Pageable pageable = PageRequest.of(page, take, Sort.by("createdAt").descending());

        String query = request.getQuery();

        Page<EnterpriseDocuments> documentsPage;

        if (query != null && !query.isBlank()) {

            documentsPage = enterpriseDocumentsRepository
                    .findByEnterpriseIdAndLegalNameContainingIgnoreCase(
                            enterprise.get().getId(),
                            query,
                            pageable
                    );

        } else {

            documentsPage = enterpriseDocumentsRepository
                    .findByEnterpriseId(
                            enterprise.get().getId(),
                            pageable
                    );
        }

        var documents = documentsPage.getContent()
                .stream()
                .map(ListEnterpriseDocumentsResponse.DocumentInfo::fromEntity)
                .toList();

        var response = ListEnterpriseDocumentsResponse.builder()
                .documents(documents)
                .total(documentsPage.getTotalElements())
                .take(take)
                .skip(skip)
                .build();

        return new ApiResponse<>(true, "Documents fetched successfully", response);
    }
}
