package br.com.harmony.DocGuard.infrastructure.config.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final Map<String, int[]> ROUTE_LIMITS = Map.of(
            "/auth",                  new int[]{100, 15},
            "/auth/forgot-password",  new int[]{3,  15},
            "/auth/reset-password",   new int[]{5,  15},
            "/auth/verification-otp", new int[]{5,  15},
            "/auth/refresh",          new int[]{20, 15},
            "/users",                 new int[]{5,  15}
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String route = request.getRequestURI();
        int[] limits = ROUTE_LIMITS.get(route);

        if (limits == null) return true; // rota sem limite configurado, deixa passar

        String key = getClientIp(request) + ":" + route;
        Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(limits[0])
                        .refillGreedy(limits[0], Duration.ofMinutes(limits[1]))
                        .build())
                .build());

        if (bucket.tryConsume(1)) return true;

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\"Too many requests\"}");
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
