package djj.spitching_be.config.auth;

import com.google.common.net.HttpHeaders;
import djj.spitching_be.Domain.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

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
                                "/loginSuccess", "/api/v1/feedback/**",
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

                                    // 이 부분을 추가: 세션 ID를 URL 파라미터로 포함
//                                    HttpSession session = request.getSession(false);
//                                    String sessionId = session != null ? session.getId() : "";

                                    // 리디렉션 URL에 세션 ID 추가
                                    //response.sendRedirect("https://spitching.vercel.app?session_id=" + sessionId);
                                    // 세션 ID 쿠키 직접 설정
                                    HttpSession session = request.getSession(false);
                                    if (session != null) {
                                        Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
                                        sessionCookie.setPath("/");
                                        sessionCookie.setDomain("spitching.store");
                                        sessionCookie.setMaxAge(21600);
                                        sessionCookie.setSecure(true);
                                        sessionCookie.setHttpOnly(true);
                                        response.addCookie(sessionCookie);

                                        // SameSite 속성은 쿠키 객체에 직접 설정할 수 없으므로 헤더로 직접 추가
                                        response.addHeader("Set-Cookie",
                                                "JSESSIONID=" + session.getId() +
                                                        "; Path=/; Domain=spitching.store; Max-Age=21600; Secure; HttpOnly; SameSite=None");
                                    }

                                    response.sendRedirect("https://www.spitching.store/");;
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

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> sameSiteCookieFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                // 먼저 필터 체인 실행
                filterChain.doFilter(request, response);

                // 응답 헤더의 Set-Cookie 값 가져오기
                Collection<String> headers = response.getHeaders("Set-Cookie");
                if (headers == null || headers.isEmpty()) {
                    return; // 쿠키가 없으면 종료
                }

                // 기존 Set-Cookie 헤더 제거
                response.setHeader("Set-Cookie", null);

                // 새로운 쿠키 헤더 추가
                for (String header : headers) {
                    // 로그로 원래 헤더 확인
                    System.out.println("Original Cookie Header: " + header);

                    // 모든 쿠키에 도메인과 SameSite 설정 추가
                    String newHeader = header;
                    if (!newHeader.contains("Domain=")) {
                        newHeader += "; Domain=spitching.store";
                    }
                    if (!newHeader.contains("SameSite=")) {
                        newHeader += "; SameSite=None";
                    }
                    if (!newHeader.contains("Secure")) {
                        newHeader += "; Secure";
                    }

                    // 로그로 수정된 헤더 확인
                    System.out.println("Modified Cookie Header: " + newHeader);

                    // 새 헤더 추가
                    response.addHeader("Set-Cookie", newHeader);
                }
            }
        });

        // 가장 높은 우선순위로 설정
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}