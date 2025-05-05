package djj.spitching_be.Controller;

import djj.spitching_be.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final UserRepository userRepository;

    /**
     * 세션 상태를 확인하는 API
     * @param request HTTP 요청 객체
     * @return 세션 상태 정보 (active 여부)
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSessionStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();

        if (session != null) {
            response.put("active", true);

            // 현재 인증 정보 확인
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() &&
                    !authentication.getPrincipal().equals("anonymousUser")) {
                response.put("authenticated", true);
            } else {
                response.put("authenticated", false);
            }
        } else {
            response.put("active", false);
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인한 사용자의 ID를 확인하는 API
     * @return 사용자 ID 정보
     */
    @GetMapping("/user/id")
    public ResponseEntity<Map<String, Object>> getUserId() {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication instanceof OAuth2AuthenticationToken &&
                !authentication.getPrincipal().equals("anonymousUser")) {

            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();

            String email = oauth2User.getAttribute("email");

            if (email != null) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    response.put("authenticated", true);
                    response.put("userId", user.getId());
                });
            }

            if (!response.containsKey("userId")) {
                response.put("authenticated", true);
                response.put("error", "사용자 ID를 찾을 수 없습니다.");
            }
        } else {
            response.put("authenticated", false);
        }

        return ResponseEntity.ok(response);
    }
}