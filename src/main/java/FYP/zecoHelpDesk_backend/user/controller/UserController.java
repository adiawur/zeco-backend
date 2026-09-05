package FYP.zecoHelpDesk_backend.user.controller;

import org.springframework.security.core.Authentication;

import FYP.zecoHelpDesk_backend.user.dto.CreateUserRequest;
import FYP.zecoHelpDesk_backend.user.dto.UpdateUserRequest;
import FYP.zecoHelpDesk_backend.user.dto.UserResponse;
import FYP.zecoHelpDesk_backend.user.service.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService service;


    // =========================
    // ADMIN
    // =========================

    @PostMapping("/admin/users")
    public UserResponse create(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return service.create(request);
    }


    @GetMapping("/admin/users")
    public List<UserResponse> getAllUsers() {
        return service.getAllUsers();
    }


    @GetMapping("/admin/users/{id}")
    public UserResponse getUserById(
            @PathVariable Long id
    ) {
        return service.getUserById(id);
    }


    @PutMapping("/admin/users/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return service.update(id, request);
    }


    @PatchMapping("/admin/users/status/{id}")
    public UserResponse changeStatus(
            @PathVariable Long id
    ) {
        return service.changeStatus(id);
    }


    @DeleteMapping("/admin/users/{id}")
    public String delete(
            @PathVariable Long id
    ) {
        service.delete(id);

        return "User deleted successfully";
    }


    @PostMapping("/admin/users/{id}/image")
    public UserResponse uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image
    ) {
        return service.uploadImage(id, image);
    }


    // =========================
    // SUPERVISOR
    // =========================

    @GetMapping("/supervisor/technicians")
    public List<UserResponse> getSupervisorTechnicians(
            Authentication authentication
    ) {

        return service.getTechniciansForSupervisor(
                authentication
        );
    }


    // =========================
    // PROFILE
    // =========================

    @GetMapping("/profile/me")
    public UserResponse getMyProfile(
            Authentication authentication
    ) {

        return service.getMyProfile(
                authentication.getName()
        );
    }


    @PutMapping("/profile/me")
    public UserResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        return service.updateMyProfile(
                authentication.getName(),
                request
        );
    }
}