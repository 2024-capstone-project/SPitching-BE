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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println("=== CustomOAuth2UserService Loaded ===");
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== loadUser() method called ===");

        OAuth2User oauth2User = super.loadUser(userRequest);
        System.out.println("=== OAuth2User Attributes ===");
        System.out.println(oauth2User.getAttributes());

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        System.out.println("Email: " + email);
        System.out.println("Name: " + name);
        System.out.println("Picture: " + picture);

        try {
            System.out.println("=== Checking if user exists ===");
            User user = userRepository.findByEmail(email)
                    .map(entity -> {
                        System.out.println("=== Existing user found, updating... ===");
                        entity.update(name, picture);
                        return userRepository.save(entity);
                    })
                    .orElseGet(() -> {
                        System.out.println("=== No existing user found, creating new user... ===");
                        return userRepository.save(User.builder()
                                .name(name)
                                .email(email)
                                .picture(picture)
                                .role(Role.USER)
                                .build());
                    });

            System.out.println("=== User saved successfully: " + user.getEmail() + " ===");

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                    oauth2User.getAttributes(),
                    "email"
            );
        } catch (Exception e) {
            System.out.println("=== Error saving user ===");
            e.printStackTrace();
            throw e;
        }
    }
}
