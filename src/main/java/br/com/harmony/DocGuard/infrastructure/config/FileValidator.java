package br.com.harmony.DocGuard.infrastructure.config;

import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class FileValidator {

    private static final long MAX_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf", "image/png", "image/jpeg"
    );

    public void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException("Arquivo não pode ser vazio", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ApiException("Arquivo não pode ultrapassar 10MB", HttpStatus.BAD_REQUEST);
        }

        try {
            Tika tika = new Tika();
            String detectedType = tika.detect(file.getBytes());
            if (!ALLOWED_TYPES.contains(detectedType)) {
                throw new ApiException("Tipo de arquivo não permitido", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            throw new ApiException("Erro ao validar arquivo", HttpStatus.BAD_REQUEST);
        }
    }
}
