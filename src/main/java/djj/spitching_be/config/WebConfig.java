package djj.spitching_be.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins(
//                        "http://localhost:5173",
//                        "https://spitching.vercel.app",
//                        "https://spitching.store"
//                )
//                .allowedMethods("*")
//                .allowedHeaders("*")
//                .allowCredentials(true);
//    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://www.spitching.store",
                        "https://spitching.store",
                        "https://spitching.vercel.app",
                        "http://localhost:5173") // 정확한 도메인만
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // 이거 있어야 쿠키 포함 가능
    }
}