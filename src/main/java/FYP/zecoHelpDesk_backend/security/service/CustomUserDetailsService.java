package FYP.zecoHelpDesk_backend.security.service;

import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid username"));

        return new org.springframework.security.core.userdetails.User(

                user.getUsername(),

                user.getPassword(),

                user.getActive(),

                true,

                true,

                true,

                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        )
                )

        );

    }

}