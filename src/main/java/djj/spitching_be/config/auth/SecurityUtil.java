package djj.spitching_be.config.auth;

import djj.spitching_be.Domain.User;
import djj.spitching_be.Repository.UserRepository;
import djj.spitching_be.config.auth.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("No authentication information.");
        }

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        return userRepository.findByEmail(oAuth2User.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
