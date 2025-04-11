package djj.spitching_be.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import djj.spitching_be.Domain.Role;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public void authenticateWithGoogle(String idToken, HttpServletRequest request) throws Exception {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new Exception("유효하지 않은 ID Token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();

            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            log.info("사용자 정보 - 이메일: {}, 이름: {}", email, name);

            // 사용자 정보 저장 또는 업데이트
            User user = userRepository.findByEmail(email)
                    .map(existingUser -> {
                        existingUser.update(name, picture);
                        return userRepository.save(existingUser);
                    })
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .name(name)
                                .email(email)
                                .picture(picture)
                                .role(Role.USER)
                                .build();
                        return userRepository.save(newUser);
                    });

            // 세션에 사용자 정보 저장
            request.getSession().setAttribute("user", user);
            log.info("사용자가 성공적으로 인증되었습니다: {}", email);
        } catch (Exception e) {
            log.error("Google 인증 중 오류 발생", e);
            throw new Exception("인증 실패: " + e.getMessage());
        }
    }
}