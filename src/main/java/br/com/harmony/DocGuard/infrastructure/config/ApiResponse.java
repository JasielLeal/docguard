package br.com.harmony.DocGuard.infrastructure.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success; // true se operação foi bem-sucedida
    private String message;  // mensagem descritiva
    private T data;          // payload opcional

}
