package com.mesproject.mescore.ext.security;

import com.mesproject.mescore.auth.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * NOTE:
 * 기존 SecurityConfig가 "/api/**" 패턴을 기준으로 되어 있어,
 * Nginx/Vite 프록시에서 "/api/core"를 제거(리라이트)하는 현재 구성과 충돌합니다.
 *
 * 이 설정은 프록시가 붙이거나/제거하는 경우 모두를 커버하도록
 * "/auth" vs "/api/auth" 같이 두 패턴을 함께 허용합니다.
 */
@Configuration
@Order(0)
public class CompatSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public CompatSecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain compatFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // login / health
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/prometheus"
                        ).permitAll()

                        // kiosk + public system master
                        .requestMatchers(
                                "/kiosk/**",
                                "/api/kiosk/**",
                                "/system/**",
                                "/api/system/**"
                        ).permitAll()

                        // admin
                        .requestMatchers(
                                "/admin/**",
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // 기존 SecurityConfig에도 csrf disable이 들어가있지만, 여기서는 명시적으로 끕니다.
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}
