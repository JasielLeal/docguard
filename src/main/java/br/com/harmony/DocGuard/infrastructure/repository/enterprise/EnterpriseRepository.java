package br.com.harmony.DocGuard.infrastructure.repository.enterprise;

import br.com.harmony.DocGuard.domain.model.Enterprise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
    Optional<Enterprise> findByCnpj(String cnpj);
    Optional<Enterprise> findByUserId(UUID userId);;
}
