package FYP.zecoHelpDesk_backend.auth.service;

import FYP.zecoHelpDesk_backend.auth.dto.LoginRequest;
import FYP.zecoHelpDesk_backend.auth.dto.LoginResponse;
import FYP.zecoHelpDesk_backend.security.jwt.JwtService;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final JwtService jwtService;


    // =========================================================
    // LOGIN
    // =========================================================

    public LoginResponse login(
            LoginRequest request
    ) {

        String loginValue =
                request.getUsername()
                        .trim();


        // =====================================================
        // FIND USER
        // =====================================================

        User user =
                userRepository
                        .findByUsernameIgnoreCase(loginValue)
                        .orElseGet(() ->

                                userRepository
                                        .findByEmailIgnoreCase(loginValue)
                                        .orElseThrow(() ->
                                                new BadCredentialsException(
                                                        "Invalid username or password"
                                                )
                                        )
                        );


        // =====================================================
        // CHECK ACTIVE ACCOUNT
        // =====================================================

        if (
                user.getActive() == null
                        ||
                        !user.getActive()
        ) {

            throw new DisabledException(
                    "Your account is inactive"
            );
        }


        // =====================================================
        // AUTHENTICATE
        // =====================================================

        try {

            authenticationManager.authenticate(

                    new UsernamePasswordAuthenticationToken(

                            user.getUsername(),

                            request.getPassword()

                    )
            );

        } catch (BadCredentialsException exception) {

            throw new BadCredentialsException(
                    "Invalid username or password"
            );
        }


        // =====================================================
        // GENERATE JWT
        // =====================================================

        String token =
                jwtService.generateToken(user);


        // =====================================================
        // RESPONSE
        // =====================================================

        return LoginResponse.builder()

                .token(token)

                .username(
                        user.getUsername()
                )

                .fullName(
                        user.getFullName()
                )

                .role(
                        user.getRole()
                )

                .userId(user.getId())

                .build();
    }
}