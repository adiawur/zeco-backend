package FYP.zecoHelpDesk_backend.user.service;

import FYP.zecoHelpDesk_backend.user.dto.CreateUserRequest;
import FYP.zecoHelpDesk_backend.user.dto.UpdateUserRequest;
import FYP.zecoHelpDesk_backend.user.dto.UserResponse;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // CREATE USER
    // =========================================================

    public UserResponse create(
            CreateUserRequest request
    ) {

        // -----------------------------------------------------
        // VALIDATE USERNAME
        // -----------------------------------------------------

        String username =
                request.getUsername() == null
                        ? null
                        : request.getUsername().trim();

        if (
                username == null
                        ||
                        username.isBlank()
        ) {

            throw new RuntimeException(
                    "Username is required"
            );
        }


        // -----------------------------------------------------
        // VALIDATE EMAIL
        // -----------------------------------------------------

        String email =
                request.getEmail() == null
                        ? null
                        : request.getEmail().trim();

        if (
                email == null
                        ||
                        email.isBlank()
        ) {

            throw new RuntimeException(
                    "Email is required"
            );
        }


        // -----------------------------------------------------
        // CHECK DUPLICATE USERNAME
        // -----------------------------------------------------

        if (
                repository.existsByUsername(
                        username
                )
        ) {

            throw new RuntimeException(
                    "Username already exists"
            );
        }


        // -----------------------------------------------------
        // CHECK DUPLICATE EMAIL
        // -----------------------------------------------------

        if (
                repository.existsByEmail(
                        email
                )
        ) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }


        // -----------------------------------------------------
        // ROLE
        // -----------------------------------------------------

        if (request.getRole() == null) {

            throw new RuntimeException(
                    "Role is required"
            );
        }


        // -----------------------------------------------------
        // PASSWORD
        // -----------------------------------------------------

        if (
                request.getPassword() == null
                        ||
                        request.getPassword().isBlank()
        ) {

            throw new RuntimeException(
                    "Password is required"
            );
        }


        // =====================================================
        // BUILD USER
        // =====================================================

        User user =
                User.builder()

                        .fullName(
                                request.getFullName()
                        )

                        .username(
                                username
                        )

                        .email(
                                email
                        )

                        .phone(
                                request.getPhone()
                        )

                        .password(
                                passwordEncoder.encode(
                                        request.getPassword()
                                )
                        )

                        .role(
                                request.getRole()
                        )

                        .specialization(
                                request.getSpecialization()
                        )

                        .zone(
                                request.getZone()
                        )

                        .imageUrl(request.getImageUrl())

                        .active(true)

                        .build();


        // =====================================================
        // SAVE
        // =====================================================

        User savedUser =
                repository.save(user);


        return toResponse(
                savedUser
        );
    }


    // =========================================================
    // GET ALL USERS
    // =========================================================

    public List<UserResponse> getAllUsers() {

        return repository.findAll()

                .stream()

                .map(this::toResponse)

                .toList();
    }


    // =========================================================
    // GET USER BY ID
    // =========================================================

    public UserResponse getUserById(
            Long id
    ) {

        User user =
                repository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        return toResponse(
                user
        );
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    public UserResponse update(
            Long id,
            UpdateUserRequest request
    ) {

        // -----------------------------------------------------
        // FIND USER
        // -----------------------------------------------------

        User user =
                repository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        // -----------------------------------------------------
        // VALIDATE USERNAME
        // -----------------------------------------------------

        String username =
                request.getUsername() == null
                        ? null
                        : request.getUsername().trim();


        if (
                username == null
                        ||
                        username.isBlank()
        ) {

            throw new RuntimeException(
                    "Username is required"
            );
        }


        // -----------------------------------------------------
        // CHECK USERNAME DUPLICATE
        // -----------------------------------------------------

        if (
                !username.equals(
                        user.getUsername()
                )
                        &&
                        repository.existsByUsername(
                                username
                        )
        ) {

            throw new RuntimeException(
                    "Username already exists"
            );
        }


        // -----------------------------------------------------
        // VALIDATE EMAIL
        // -----------------------------------------------------

        String email =
                request.getEmail() == null
                        ? null
                        : request.getEmail().trim();


        if (
                email == null
                        ||
                        email.isBlank()
        ) {

            throw new RuntimeException(
                    "Email is required"
            );
        }


        // -----------------------------------------------------
        // CHECK EMAIL DUPLICATE
        // -----------------------------------------------------

        if (
                !email.equals(
                        user.getEmail()
                )
                        &&
                        repository.existsByEmail(
                                email
                        )
        ) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }


        // -----------------------------------------------------
        // ROLE
        // -----------------------------------------------------

        if (request.getRole() == null) {

            throw new RuntimeException(
                    "Role is required"
            );
        }


        // =====================================================
        // UPDATE USER
        // =====================================================

        user.setFullName(
                request.getFullName()
        );

        user.setUsername(
                username
        );

        user.setEmail(
                email
        );

        user.setPhone(
                request.getPhone()
        );

        user.setRole(
                request.getRole()
        );

        user.setSpecialization(
                request.getSpecialization()
        );

        user.setImageUrl(request.getImageUrl());

        user.setZone(
                request.getZone()
        );


        // =====================================================
        // SAVE
        // =====================================================

        User updatedUser =
                repository.save(user);


        return toResponse(
                updatedUser
        );
    }


    // =========================================================
    // CHANGE USER STATUS
    // =========================================================

    public UserResponse changeStatus(
            Long id
    ) {

        User user =
                repository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        boolean currentStatus =
                Boolean.TRUE.equals(
                        user.getActive()
                );


        user.setActive(
                !currentStatus
        );


        User updatedUser =
                repository.save(user);


        return toResponse(
                updatedUser
        );
    }


    // =========================================================
    // DELETE USER
    // =========================================================

    public void delete(
            Long id
    ) {

        if (
                !repository.existsById(id)
        ) {

            throw new RuntimeException(
                    "User not found"
            );
        }


        repository.deleteById(id);
    }


    public UserResponse uploadImage(
            Long id,
            MultipartFile image
    ) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (image == null || image.isEmpty()) {

            throw new RuntimeException(
                    "Please select an image"
            );

        }

        String contentType = image.getContentType();

        if (
                contentType == null ||
                        !contentType.startsWith("image/")
        ) {

            throw new RuntimeException(
                    "Only image files are allowed"
            );

        }

        try {

            Path uploadPath =
                    Paths.get("uploads/users");

            Files.createDirectories(
                    uploadPath
            );

            String extension = "";

            String originalName =
                    image.getOriginalFilename();

            if (
                    originalName != null &&
                            originalName.contains(".")
            ) {

                extension =
                        originalName.substring(
                                originalName.lastIndexOf(".")
                        );

            }

            String fileName =
                    "USER-" +
                            user.getId() +
                            "-" +
                            UUID.randomUUID() +
                            extension;

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.write(
                    filePath,
                    image.getBytes()
            );

            user.setImageUrl(
                    "/uploads/users/" + fileName
            );

            User savedUser =
                    repository.save(user);

            return toResponse(
                    savedUser
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload image"
            );

        }

    }

    // =========================================================
    // USER RESPONSE MAPPER
    // =========================================================

    private UserResponse toResponse(
            User user
    ) {

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .specialization(user.getSpecialization())
                .zone(user.getZone())
                .imageUrl(user.getImageUrl())
                .active(user.getActive())
                .build();
    }

    public UserResponse getMyProfile(
            String username
    ) {

        User user =
                repository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );

        return toResponse(user);
    }

    public UserResponse updateMyProfile(
            String username,
            UpdateUserRequest request
    ) {

        User user =
                repository.findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        // UPDATE BASIC INFORMATION

        user.setFullName(
                request.getFullName()
        );


        user.setUsername(
                request.getUsername()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPhone(
                request.getPhone()
        );

        // =====================================================
        // UPDATE TECHNICIAN INFORMATION
        // =====================================================

        user.setSpecialization(
                request.getSpecialization()
        );

        user.setZone(
                request.getZone()
        );


        // =====================================================
        // UPDATE PROFILE IMAGE URL
        // =====================================================

        if (
                request.getImageUrl() != null
                        &&
                        !request.getImageUrl().isBlank()
        ) {

            user.setImageUrl(
                    request.getImageUrl()
            );

        }


        // =====================================================
        // SAVE
        // =====================================================

        User savedUser =
                repository.save(user);


        return toResponse(
                savedUser
        );
    }

    }