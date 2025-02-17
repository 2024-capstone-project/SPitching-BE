package djj.spitching_be.config.auth;


import djj.spitching_be.Domain.Role;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("=== CustomOAuth2UserService Loaded ===");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== loadUser method called ===");
        OAuth2User oauth2User = super.loadUser(userRequest);
        log.info("OAuth2User loaded: {}", oauth2User.getAttributes());
        return oauth2User;
    }

    @Transactional
    public User saveOrUpdateUser(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        log.info("Saving/Updating user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .map(entity -> {
                    log.info("Updating existing user: {}", email);
                    entity.update(name, picture);
                    return userRepository.save(entity);
                })
                .orElseGet(() -> {
                    log.info("Creating new user: {}", email);
                    User newUser = User.builder()
                            .name(name)
                            .email(email)
                            .picture(picture)
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });

        log.info("User saved with ID: {}", user.getId());
        return user;
    }
}