package FYP.zecoHelpDesk_backend.user.service;

import FYP.zecoHelpDesk_backend.user.dto.CreateUserRequest;
import FYP.zecoHelpDesk_backend.user.dto.UserResponse;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(CreateUserRequest request) {

        if (repository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .specialization(request.getSpecialization())
                .zone(request.getZone())
                .active(true)
                .build();

        User savedUser = repository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole())
                .specialization(savedUser.getSpecialization())
                .zone(savedUser.getZone())
                .active(savedUser.getActive())
                .build();
    }



    public List<UserResponse> getAllUsers() {

        return repository.findAll()

                .stream()

                .map(user -> UserResponse.builder()

                        .id(user.getId())

                        .fullName(user.getFullName())

                        .username(user.getUsername())

                        .email(user.getEmail())

                        .phone(user.getPhone())

                        .role(user.getRole())

                        .specialization(user.getSpecialization())

                        .zone(user.getZone())

                        .active(user.getActive())

                        .build())

                .toList();

    }

    public UserResponse getUserById(Long id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return UserResponse.builder()

                .id(user.getId())

                .fullName(user.getFullName())

                .username(user.getUsername())

                .email(user.getEmail())

                .phone(user.getPhone())

                .role(user.getRole())

                .specialization(user.getSpecialization())

                .zone(user.getZone())

                .active(user.getActive())

                .build();

    }

}