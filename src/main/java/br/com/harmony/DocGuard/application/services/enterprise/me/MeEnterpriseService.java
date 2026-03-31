package br.com.harmony.DocGuard.application.services.enterprise.me;


import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.enterprise.EnterpriseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MeEnterpriseService {

    private final EnterpriseRepository enterpriseRepository;

    public MeEnterpriseService(EnterpriseRepository enterpriseRepository) {
        this.enterpriseRepository = enterpriseRepository;
    }

    public ApiResponse<MeResponse> execute(User user) {

        var enterprise = enterpriseRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApiException("Enterprise not found", HttpStatus.NOT_FOUND));

        return new ApiResponse<>(true, "Enterprise data", new MeResponse(enterprise));
    }
}
