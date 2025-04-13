package djj.spitching_be.config.auth;

import djj.spitching_be.Domain.User;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
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
                .cors(cors -> cors.configure(http)) // 람다 표현식으로 수정
                //.cors(cors -> cors.configurationSource(corsConfigurationSource())) // 명시적으로 CORS 설정 사용
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/images/**",
                                "/js/**", "/h2-console/**", "/login",
                                "/loginSuccess", "api/v1/feedback/gesture",
                                "/api/v1/login/google", "/api/v1/logout", "/health", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/api/v1/login/google")
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
                )
                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/api/v1/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            try {
                                response.getWriter().write("로그아웃 성공");
                            } catch (IOException e) {
                                log.error("로그아웃 응답 작성 중 오류", e);
                            }
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "https://spitching.vercel.app"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}