package br.com.harmony.DocGuard.services.auth;

import br.com.harmony.DocGuard.application.services.auth.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Injeta os @Value manualmente sem precisar subir o Spring
        ReflectionTestUtils.setField(jwtService, "secret", "chave-de-teste-super-secreta-com-32-bytes!!");
        ReflectionTestUtils.setField(jwtService, "expiration", 900000L); // 15 min
    }

    // ✅ Token gerado não deve ser nulo ou vazio
    @Test
    void shouldGenerateNonEmptyToken() {
        String token = jwtService.generateToken("user-123", "MEMBER", "FREE");

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
    }

    // ✅ Token deve conter 3 partes (header.payload.signature)
    @Test
    void shouldGenerateValidJwtFormat() {
        String token = jwtService.generateToken("user-123", "MEMBER", "FREE");

        assertThat(token.split("\\.")).hasSize(3);
    }

    // ✅ UserId extraído deve ser o mesmo que foi gerado
    @Test
    void shouldExtractCorrectUserId() {
        String token = jwtService.generateToken("user-123", "MEMBER", "FREE");

        String extractedUserId = jwtService.extractEmail(token);

        assertThat(extractedUserId).isEqualTo("user-123");
    }

    // ✅ Tokens gerados para usuários diferentes devem ser diferentes
    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = jwtService.generateToken("user-123", "MEMBER", "FREE");
        String token2 = jwtService.generateToken("user-456", "MEMBER", "FREE");

        assertThat(token1).isNotEqualTo(token2);
    }

    // ✅ Tokens gerados para o mesmo usuário em momentos diferentes devem ser diferentes
    @Test
    void shouldGenerateDifferentTokensForSameUserAtDifferentTimes() throws InterruptedException {
        String token1 = jwtService.generateToken("user-123", "MEMBER", "FREE");
        Thread.sleep(1000); // 1 segundo garante timestamp diferente
        String token2 = jwtService.generateToken("user-123", "MEMBER", "FREE");

        assertThat(token1).isNotEqualTo(token2);
    }

    // ❌ Token expirado deve lançar exceção
    @Test
    void shouldThrowWhenTokenIsExpired() {
        // Seta expiração negativa para simular token já expirado
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String expiredToken = jwtService.generateToken("user-123", "MEMBER", "FREE");

        assertThatThrownBy(() -> jwtService.extractEmail(expiredToken))
                .isInstanceOf(Exception.class);
    }

    // ❌ Token inválido deve lançar exceção
    @Test
    void shouldThrowWhenTokenIsInvalid() {
        assertThatThrownBy(() -> jwtService.extractEmail("token.invalido.aqui"))
                .isInstanceOf(Exception.class);
    }

    // ❌ Token assinado com secret diferente deve lançar exceção
    @Test
    void shouldThrowWhenTokenIsSignedWithDifferentSecret() {
        // Gera token com secret diferente
        JwtService outroService = new JwtService();
        ReflectionTestUtils.setField(outroService, "secret", "outra-chave-completamente-diferente-aqui!!");
        ReflectionTestUtils.setField(outroService, "expiration", 900000L);

        String tokenDeOutroServico = outroService.generateToken("user-123", "MEMBER", "FREE");

        assertThatThrownBy(() -> jwtService.extractEmail(tokenDeOutroServico))
                .isInstanceOf(Exception.class);
    }
}
