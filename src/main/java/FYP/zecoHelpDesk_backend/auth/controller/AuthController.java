package FYP.zecoHelpDesk_backend.auth.controller;

import FYP.zecoHelpDesk_backend.auth.dto.LoginRequest;
import FYP.zecoHelpDesk_backend.auth.dto.LoginResponse;
import FYP.zecoHelpDesk_backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(

            @Valid
            @RequestBody
            LoginRequest request

    ){

        return authService.login(request);

    }

    @GetMapping("/me")
    public String me(){

        return "Authenticated";

    }

}