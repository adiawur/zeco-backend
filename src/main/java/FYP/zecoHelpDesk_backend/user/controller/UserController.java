package FYP.zecoHelpDesk_backend.user.controller;

import FYP.zecoHelpDesk_backend.user.dto.CreateUserRequest;
import FYP.zecoHelpDesk_backend.user.dto.UserResponse;
import FYP.zecoHelpDesk_backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService service;

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return service.getAllUsers();

    }

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable Long id
    ) {

        return service.getUserById(id);

    }

}