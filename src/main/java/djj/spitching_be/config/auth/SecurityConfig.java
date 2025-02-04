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


    //CORS 설정 추가
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // react app의 Local url
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true); // 인증 정보를 쿠키와 함께 보내도록 허용
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 보안 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/board/**","/pet/**", "/chat/**","/api/**").authenticated()  // 모든 API는 로그인 인증 필요
                        .requestMatchers("/", "/login/**", "/oauth2/**").permitAll() // 인증 없이 접근 가능한 경로 : 로그인 페이지
                        .anyRequest().authenticated() // 나머지 경로는 인증이 필요
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")  // 로그인 페이지 경로 추가
                        // .defaultSuccessUrl("/loginSuccess")  // 로그인 성공 시 리다이렉트 경로
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // 커스텀 OAuth2 서비스 사용
                        .successHandler(((request, response, authentication) -> {
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            // 세션 설정
                            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                            response.sendRedirect("http://localhost:8080/loginSuccess");
                        }))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpStatus.OK.value());
                            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.getWriter().write("Logout successful");
                            response.getWriter().flush();
                        })
                );

        return http.build();
    }

    // 로그인 성공 핸들러
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return ((request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().flush();
        });
    }
}