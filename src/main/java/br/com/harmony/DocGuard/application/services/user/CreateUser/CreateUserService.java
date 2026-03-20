package br.com.harmony.DocGuard.application.services.user.CreateUser;

import br.com.harmony.DocGuard.domain.model.User;
import br.com.harmony.DocGuard.infrastructure.repository.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.user.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Encoder;

@Service
public class CreateUserService {

    private final UserRepository repository;

    public CreateUserService(UserRepository repository) {
        this.repository = repository;
    }

    public ApiResponse<Void> execute(CreateUserRequest request) {

        if (request.getFirstName() == null || request.getLastName() == null || request.getEmail() == null) {
            return new ApiResponse<>(false, "First name, last name and email are required", null);
        }

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return new ApiResponse<>(false, "Email already exists", null);
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(User.Role.MEMBER);
        user.setPlan(User.Plan.FREE);

        repository.save(user);

        return new ApiResponse<>(true, "User created successfully", null);
    }
}
