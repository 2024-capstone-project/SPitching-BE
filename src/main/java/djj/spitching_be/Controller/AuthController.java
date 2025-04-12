package djj.spitching_be.Controller;

import djj.spitching_be.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/login")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<String> loginWithGoogle(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String accessToken = body.get("idToken");
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body("ID token is missing");
        }

        try {
            authService.authenticateWithGoogle(accessToken, request);
            return ResponseEntity.ok("로그인 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패: " + e.getMessage());
        }
    }
}
