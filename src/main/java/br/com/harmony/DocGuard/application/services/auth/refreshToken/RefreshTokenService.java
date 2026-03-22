package br.com.harmony.DocGuard.application.services.auth.refreshToken;

import br.com.harmony.DocGuard.infrastructure.repository.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final SessionRepository sessionRepository;

    public RefreshTokenService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

   public ApiResponse<Void> execute(RefreshTokenRequest request){
       var session = sessionRepository.findByRefreshToken(request.getRefreshToken());

       if(session.isEmpty()){
           return new ApiResponse<>(false, "Invalid refresh token", null);
       }

       var currentSession = session.get();

       if(currentSession.isExpired()){
           return new ApiResponse<>(false, "Invalid refresh token", null);
       }

       if(currentSession.isRevoked()){
           return new ApiResponse<>(false, "Invalid refresh token", null);
       }

       currentSession.setRevoked(true);

       return new ApiResponse<>(true, "Refresh token updated", null);

   }
}
