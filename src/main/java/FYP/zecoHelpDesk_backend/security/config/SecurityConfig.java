package FYP.zecoHelpDesk_backend.security.config;

import FYP.zecoHelpDesk_backend.security.jwt.JwtFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }


    // =========================================================
    // CORS
    // =========================================================

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:4200"
                )
        );


        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );


        configuration.setAllowedHeaders(
                List.of(
                        "*"
                )
        );


        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );


        configuration.setAllowCredentials(false);


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {


        http


                // =================================================
                // CORS
                // =================================================

                .cors(cors -> {
                })


                // =================================================
                // CSRF
                // =================================================

                .csrf(csrf ->
                        csrf.disable()
                )


                // =================================================
                // SESSION
                // =================================================

                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth


                        // -------------------------------------------------
                        // OPTIONS / PREFLIGHT
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()


                        // -------------------------------------------------
                        // AUTH
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()


                        // -------------------------------------------------
                        // PUBLIC INCIDENT REPORT
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/incidents/report"
                        ).permitAll()


                        // -------------------------------------------------
                        // PUBLIC INCIDENT TRACKING
                        // -------------------------------------------------

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/incidents/track"
                        ).permitAll()


                        // -------------------------------------------------
                        // ADMIN
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")


                        // -------------------------------------------------
                        // SUPERVISOR
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/supervisor/**"
                        ).hasRole("SUPERVISOR")


                        // -------------------------------------------------
                        // TECHNICIAN
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/technician/**"
                        ).hasRole("TECHNICIAN")


                        // -------------------------------------------------
                        // OTHER INCIDENT ENDPOINTS
                        // -------------------------------------------------
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/incidents/complaint"
                        ).permitAll()
                        .requestMatchers(
                                "/api/incidents/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPERVISOR",
                                "TECHNICIAN"
                        )


                        // -------------------------------------------------
                        // NOTIFICATIONS
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/notifications/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPERVISOR",
                                "TECHNICIAN"
                        )


                        // -------------------------------------------------
                        // REPORTS
                        // -------------------------------------------------

                        .requestMatchers(
                                "/api/admin/reports/**"
                        ).hasAnyRole(
                                "ADMIN",
                                "SUPERVISOR"
                        )


                        // -------------------------------------------------
                        // EVERYTHING ELSE
                        // -------------------------------------------------

                        .anyRequest()
                        .authenticated()
                )


                // =================================================
                // EXCEPTION HANDLING
                // =================================================

                .exceptionHandling(exception ->

                        exception


                                // =============================================
                                // 401
                                // =============================================

                                .authenticationEntryPoint(
                                        (request, response, authException) -> {

                                            response.setStatus(
                                                    HttpStatus.UNAUTHORIZED.value()
                                            );


                                            response.setContentType(
                                                    "application/json"
                                            );


                                            response.getWriter().write(
                                                    """
                                                    {
                                                      "status": 401,
                                                      "error": "UNAUTHORIZED",
                                                      "message": "Authentication is required to access this resource."
                                                    }
                                                    """
                                            );
                                        }
                                )


                                // =============================================
                                // 403
                                // =============================================

                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {

                                            response.setStatus(
                                                    HttpStatus.FORBIDDEN.value()
                                            );


                                            response.setContentType(
                                                    "application/json"
                                            );


                                            response.getWriter().write(
                                                    """
                                                    {
                                                      "status": 403,
                                                      "error": "FORBIDDEN",
                                                      "message": "You do not have permission to access this resource."
                                                    }
                                                    """
                                            );
                                        }
                                )
                )


                // =================================================
                // JWT FILTER
                // =================================================

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}