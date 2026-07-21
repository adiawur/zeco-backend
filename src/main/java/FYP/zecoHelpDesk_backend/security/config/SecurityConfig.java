package FYP.zecoHelpDesk_backend.security.config;

import FYP.zecoHelpDesk_backend.security.jwt.JwtFilter;
import FYP.zecoHelpDesk_backend.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();

    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        ))

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**",
                                "/api/incidents/report"
                        ).permitAll()

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/supervisor/**")
                        .hasRole("SUPERVISOR")

                        .requestMatchers("/api/technician/**")
                        .hasRole("TECHNICIAN")

                        .anyRequest()
                        .authenticated()

                )

                .addFilterBefore(

                        jwtFilter,

                        UsernamePasswordAuthenticationFilter.class

                );

        return http.build();

    }

}