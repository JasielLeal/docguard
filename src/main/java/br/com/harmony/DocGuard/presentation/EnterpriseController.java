package br.com.harmony.DocGuard.presentation;

import br.com.harmony.DocGuard.application.services.enterprise.create.CreateEnterpriseRequest;
import br.com.harmony.DocGuard.application.services.enterprise.create.CreateEnterpriseService;
import br.com.harmony.DocGuard.application.services.enterprise.me.MeEnterpriseService;
import br.com.harmony.DocGuard.application.services.enterprise.me.MeResponse;
import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/enterprise")
@RestController
public class EnterpriseController {

    private final CreateEnterpriseService createEnterpriseService;
    private final MeEnterpriseService meEnterpriseServiceService;

    public EnterpriseController(CreateEnterpriseService createEnterpriseService, MeEnterpriseService meEnterpriseServiceService) {
        this.createEnterpriseService = createEnterpriseService;
        this.meEnterpriseServiceService = meEnterpriseServiceService;
    }

    @PostMapping("/create")
    public ApiResponse<Void> createEnterprise(@RequestBody @Valid CreateEnterpriseRequest request,
                                              Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return createEnterpriseService.execute(request, user);
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return meEnterpriseServiceService.execute(user);
    }
}
