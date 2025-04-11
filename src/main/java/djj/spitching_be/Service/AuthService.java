package djj.spitching_be.Service;

import djj.spitching_be.Domain.Role;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public void authenticateWithGoogle(String accessToken, HttpServletRequest request) throws Exception {
        // Google API를 사용하여 사용자 정보 가져오기
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Failed to fetch user info from Google");
        }

        Map<String, Object> userInfo = response.getBody();
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        // 사용자 정보 저장 또는 업데이트
        User user = userRepository.findByEmail(email)
                .map(existingUser -> {
                    existingUser.update(name, picture);
                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .picture(picture)
                            .role(Role.USER)
                            .build();
                    return newUser;
                });

        userRepository.save(user);

        // 세션에 사용자 정보 저장
        request.getSession().setAttribute("user", user);
    }
}
