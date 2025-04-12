package djj.spitching_be.config.auth;

import djj.spitching_be.Domain.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/images/**",
                                "/js/**", "/h2-console/**", "/login",
                                "/loginSuccess", "api/v1/feedback/gesture", "/api/v1/login/google").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/api/v1/login/google")
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String email = oauth2User.getAttribute("email");
                            String name = oauth2User.getAttribute("name");
                            String picture = oauth2User.getAttribute("picture");

                            log.info("User Info - email: {}, name: {}", email, name);

                            // CustomOAuth2UserService를 직접 호출
                            try {
                                User user = customOAuth2UserService.saveOrUpdateUser(oauth2User);
                                log.info("User saved/updated successfully: {}", user.getEmail());
                            } catch (Exception e) {
                                log.error("Error saving user", e);
                            }

                            response.sendRedirect("https://spitching.vercel.app");
                        })
                );

        return http.build();
    }
}