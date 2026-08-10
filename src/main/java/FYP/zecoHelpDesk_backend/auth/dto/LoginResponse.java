package FYP.zecoHelpDesk_backend.auth.dto;

import FYP.zecoHelpDesk_backend.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;

    private String username;

    private String fullName;

    private Role role;

    private Long userId;

}