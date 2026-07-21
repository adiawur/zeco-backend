package FYP.zecoHelpDesk_backend.security.config;

import FYP.zecoHelpDesk_backend.user.entity.Role;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        boolean adminExists = userRepository.findAll()

                .stream()

                .anyMatch(user -> user.getRole() == Role.ADMIN);

        if (!adminExists) {

            User admin = User.builder()

                    .fullName("System Administrator")

                    .username("admin@zeco")

                    .email("admin@zeco.go.tz")

                    .phone("0712345678")

                    .password(
                            passwordEncoder.encode("admin##123")
                    )

                    .role(Role.ADMIN)

                    .active(true)

                    .build();

            userRepository.save(admin);

            System.out.println("=========================================");
            System.out.println(" Default Admin Created Successfully");
            System.out.println(" Username : admin@zeco");
            System.out.println(" Password : admin##123");
            System.out.println("=========================================");

        }

    }

}