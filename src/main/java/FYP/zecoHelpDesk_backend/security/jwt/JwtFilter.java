package FYP.zecoHelpDesk_backend.security.jwt;

import FYP.zecoHelpDesk_backend.security.service.CustomUserDetailsService;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final CustomUserDetailsService userDetailsService;

    private final UserRepository userRepository;


    // =========================================================
    // PUBLIC ENDPOINTS
    // JWT FILTER WILL NOT RUN FOR THESE
    // =========================================================

    @Override
    protected boolean shouldNotFilter(
            @NonNull HttpServletRequest request
    ) {

        String path =
                request.getServletPath();

        String method =
                request.getMethod();


        // -----------------------------------------------------
        // AUTH
        // -----------------------------------------------------

        if (path.startsWith("/api/auth/")) {

            return true;
        }

        // =========================================================
// PUBLIC INCIDENT REPORT
// =========================================================

        if (
                method.equalsIgnoreCase("POST")
                        &&
                        path.equals("/api/incidents/report")
        ) {

            return true;
        }


// =========================================================
// PUBLIC INCIDENT TRACKING
// =========================================================

        if (
                method.equalsIgnoreCase("POST")
                        &&
                        path.equals("/api/incidents/track")
        ) {

            return true;
        }


// =========================================================
// PUBLIC INCIDENT COMPLAINT / FEEDBACK
// =========================================================

        if (
                method.equalsIgnoreCase("POST")
                        &&
                        path.equals("/api/incidents/complaint")
        ) {

            return true;
        }


        // -----------------------------------------------------
        // CORS PREFLIGHT
        // -----------------------------------------------------

        if (
                method.equalsIgnoreCase("OPTIONS")
        ) {

            return true;
        }


        // -----------------------------------------------------
        // ALL OTHER REQUESTS
        // JWT FILTER RUNS
        // -----------------------------------------------------

        return false;
    }


    // =========================================================
    // JWT FILTER
    // =========================================================

    @Override
    protected void doFilterInternal(

            @NonNull HttpServletRequest request,

            @NonNull HttpServletResponse response,

            @NonNull FilterChain filterChain

    ) throws ServletException, IOException {


        // =====================================================
        // GET AUTHORIZATION HEADER
        // =====================================================

        String authHeader =
                request.getHeader("Authorization");


        // =====================================================
        // NO TOKEN
        // =====================================================

        if (
                authHeader == null
                        ||
                        !authHeader.startsWith("Bearer ")
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =====================================================
        // EXTRACT TOKEN
        // =====================================================

        String token =
                authHeader.substring(7).trim();


        // =====================================================
        // EMPTY TOKEN
        // =====================================================

        if (token.isEmpty()) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        try {

            // =================================================
            // EXTRACT USERNAME
            // =================================================

            String username =
                    jwtService.extractUsername(token);


            // =================================================
            // CHECK USERNAME
            // =================================================

            if (
                    username != null
                            &&
                            SecurityContextHolder
                                    .getContext()
                                    .getAuthentication()
                                    == null
            ) {


                // =============================================
                // LOAD USER DETAILS
                // =============================================

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        username
                                );


                // =============================================
                // FIND USER
                // =============================================

                User user =
                        userRepository
                                .findByUsername(username)
                                .orElse(null);


                // =============================================
                // VALIDATE USER + TOKEN
                // =============================================

                if (
                        user != null
                                &&
                                Boolean.TRUE.equals(
                                        user.getActive()
                                )
                                &&
                                jwtService.isTokenValid(
                                        token,
                                        user
                                )
                ) {


                    // =========================================
                    // CREATE AUTHENTICATION
                    // =========================================

                    UsernamePasswordAuthenticationToken authentication =

                            new UsernamePasswordAuthenticationToken(

                                    userDetails,

                                    null,

                                    userDetails.getAuthorities()

                            );


                    // =========================================
                    // REQUEST DETAILS
                    // =========================================

                    authentication.setDetails(

                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)

                    );


                    // =========================================
                    // SET SECURITY CONTEXT
                    // =========================================

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );
                }
            }

        } catch (Exception e) {

            // =================================================
            // INVALID TOKEN
            // =================================================

            SecurityContextHolder
                    .clearContext();

        }


        // =====================================================
        // CONTINUE
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}