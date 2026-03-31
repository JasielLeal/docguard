package br.com.harmony.DocGuard.infrastructure.repository.optToken;

import br.com.harmony.DocGuard.domain.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<OtpToken, UUID> {
    Optional<OtpToken> findOtpTokenByCode(String code);
    List<OtpToken> findUserOtpTokenByCode(String code);
    List<OtpToken> findAllByUser_IdAndTypeAndUsedFalse(UUID userId, OtpToken.Type type);

    List<OtpToken> findAllByUserIdAndTypeAndUsedFalse(UUID userId, OtpToken.Type type);
}
