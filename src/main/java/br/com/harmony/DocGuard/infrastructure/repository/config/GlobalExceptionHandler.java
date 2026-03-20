package br.com.harmony.DocGuard.infrastructure.repository.config;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException ex) {
        return new ApiResponse<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ApiResponse<>(false, ex.getMessage(), null);
    }
}
