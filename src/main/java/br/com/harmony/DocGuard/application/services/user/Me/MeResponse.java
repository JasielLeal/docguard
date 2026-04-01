package br.com.harmony.DocGuard.application.services.user.Me;


import br.com.harmony.DocGuard.domain.model.User;
import lombok.Getter;

@Getter
public class MeResponse {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String role;
    private final String plan;
    private final String status;

    public MeResponse(User user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole().name();
        this.plan = user.getPlan().name();
        this.status = user.getStatus().name();
    }
}
