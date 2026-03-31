package br.com.harmony.DocGuard.application.services.user.Me;

import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import org.springframework.stereotype.Service;

@Service
public class MeService {
    public ApiResponse<MeResponse> execute(User user) {
        return new ApiResponse<>(true, "User data", new MeResponse(user));
    }
}
