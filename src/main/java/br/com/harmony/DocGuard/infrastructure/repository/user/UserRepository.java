package br.com.harmony.DocGuard.infrastructure.repository.user;

import br.com.harmony.DocGuard.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
