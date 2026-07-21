package FYP.zecoHelpDesk_backend.user.dto;

import FYP.zecoHelpDesk_backend.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String password;

    private Role role;

    private String specialization;

    private String zone;

}