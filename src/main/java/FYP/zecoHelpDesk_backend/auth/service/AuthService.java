package FYP.zecoHelpDesk_backend.auth.service;

import FYP.zecoHelpDesk_backend.auth.dto.LoginRequest;
import FYP.zecoHelpDesk_backend.auth.dto.LoginResponse;
import FYP.zecoHelpDesk_backend.security.jwt.JwtService;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request){

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getUsername(),

                        request.getPassword()

                )

        );

        User user = userRepository.findByUsername(

                request.getUsername()

        ).orElseThrow();

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()

                .token(token)

                .username(user.getUsername())

                .fullName(user.getFullName())

                .role(user.getRole())

                .build();

    }

}