package com.gills_dream.global.config;

import com.gills_dream.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PERMITTED_PATTERNS = {
            "/",
            "/v1/member/reissue",
            "/v1/map/**",
            "/v1/place/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/resources/**",
            "/v1/member/oauth/kakao/login",
            "/v1/member/oauth/apple/login",
            "/v1/plan/template/*/generate",
            "/v1/plan/template/*/destination",
            "/v1/plan/template/*/mood",
            "/v1/plan/template/*/videos",
            "/v1/plan/template/*/style",
            "/v1/plan/template/*/plan-summary",
            "/v1/plan/template/*/duration",
            "/v1/plan/template/*/companion",
            "/v1/plan/template/*/plan",
            "/v1/plan/template/region",
            "/v1/member/check"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .cors(cors-> cors.configurationSource(CorsConfig.corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .formLogin((auth) -> auth.disable())
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMITTED_PATTERNS).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}