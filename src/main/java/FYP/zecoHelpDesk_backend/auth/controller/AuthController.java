package FYP.zecoHelpDesk_backend.auth.controller;

import FYP.zecoHelpDesk_backend.auth.dto.LoginRequest;
import FYP.zecoHelpDesk_backend.auth.dto.LoginResponse;
import FYP.zecoHelpDesk_backend.auth.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public LoginResponse login(

            @Valid
            @RequestBody
            LoginRequest request

    ) {

        return authService.login(request);
    }


    // =========================================================
    // CHECK AUTHENTICATION
    // =========================================================

    @GetMapping("/me")
    public String me() {

        return "Authenticated";
    }


    // =========================================================
    // INVALID CREDENTIALS
    // =========================================================

    @ExceptionHandler(
            BadCredentialsException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleBadCredentials(
            BadCredentialsException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        Map.of(
                                "status",
                                401,

                                "error",
                                "INVALID_CREDENTIALS",

                                "message",
                                "Invalid username or password."
                        )
                );
    }


    // =========================================================
    // INACTIVE ACCOUNT
    // =========================================================

    @ExceptionHandler(
            DisabledException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleDisabledAccount(
            DisabledException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        Map.of(
                                "status",
                                403,

                                "error",
                                "ACCOUNT_INACTIVE",

                                "message",
                                "Your account is inactive. Please contact the administrator."
                        )
                );
    }
}