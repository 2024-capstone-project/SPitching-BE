package djj.spitching_be.config.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://spitching.store",
                "https://www.spitching.store"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .requiresChannel(channel -> channel
                        .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                        .requiresSecure())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/board/**","/pet/**", "/chat/**","/api/**").authenticated()
//                        .requestMatchers("/", "/login/**", "/oauth2/**", "/health").permitAll()
//                        .anyRequest().authenticated()
                          .requestMatchers("/**").permitAll() // ✅ 모든 요청을 로그인 없이 허용
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(((request, response, authentication) -> {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

                            // 프로덕션과 로컬 환경에 따른 리다이렉트 URI 분기
                            String redirectUri = request.getServerName().contains("localhost")
                                    ? "http://localhost:8080/loginSuccess"
                                    : "https://spitching.store/loginSuccess";

                            response.sendRedirect(redirectUri);
                        }))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            String origin = request.getServerName().contains("localhost")
                                    ? "http://localhost:3000"
                                    : "https://spitching.store";
                            response.setHeader("Access-Control-Allow-Origin", origin);
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.getWriter().write("Logout successful");
                            response.getWriter().flush();
                        })
                );

        return http.build();
    }
}