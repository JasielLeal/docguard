package br.com.harmony.DocGuard.infrastructure.repository.enterpriseDocuments;

import br.com.harmony.DocGuard.domain.model.EnterpriseDocuments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnterpriseDocumentsRepository extends JpaRepository<EnterpriseDocuments, UUID> {
    Page<EnterpriseDocuments> findByEnterpriseId(UUID enterpriseId, Pageable pageable);

    Page<EnterpriseDocuments> findByEnterpriseIdAndLegalNameContainingIgnoreCase(
            UUID enterpriseId,
            String legalName,
            Pageable pageable
    );
}
