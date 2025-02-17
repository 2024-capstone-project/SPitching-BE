package djj.spitching_be.config.auth;


import djj.spitching_be.Domain.Role;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 1. 구글에서 가져온 정보 로깅
        System.out.println("=== OAuth2User Attributes ===");
        System.out.println(oauth2User.getAttributes());

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        // 2. 정보 추출 확인 로깅
        System.out.println("=== Extracted User Info ===");
        System.out.println("Email: " + email);
        System.out.println("Name: " + name);
        System.out.println("Picture: " + picture);

        try {
            User user = userRepository.findByEmail(email)
                    .map(entity -> entity.update(name, picture))
                    .orElse(User.builder()
                            .name(name)
                            .email(email)
                            .picture(picture)
                            .role(Role.USER)
                            .build());

            // 3. 저장 시도 로깅
            System.out.println("=== Trying to save user ===");
            User savedUser = userRepository.save(user);
            System.out.println("User saved successfully: " + savedUser.getEmail());

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                    oauth2User.getAttributes(),
                    "email"
            );
        } catch (Exception e) {
            // 4. 에러 발생 시 로깅
            System.out.println("=== Error saving user ===");
            e.printStackTrace();
            throw e;
        }
    }
}