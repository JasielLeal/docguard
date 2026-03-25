package br.com.harmony.DocGuard.application.services.auth.refreshToken;

import br.com.harmony.DocGuard.infrastructure.config.ApiException;
import br.com.harmony.DocGuard.infrastructure.config.ApiResponse;
import br.com.harmony.DocGuard.infrastructure.repository.session.SessionRepository;
import org.springframework.http.HttpStatus;
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
           throw new ApiException("Invalid refresh token", HttpStatus.BAD_REQUEST);
       }

       var currentSession = session.get();

       if(currentSession.isExpired()){
           throw new ApiException("Invalid refresh token", HttpStatus.BAD_REQUEST);
       }

       if(currentSession.isRevoked()){
           throw new ApiException("Invalid refresh token", HttpStatus.BAD_REQUEST);
       }

       currentSession.setRevoked(true);

       return new ApiResponse<>(true, "Refresh token updated", null);

   }
}
