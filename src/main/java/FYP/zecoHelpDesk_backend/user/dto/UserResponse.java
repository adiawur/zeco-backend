package FYP.zecoHelpDesk_backend.user.dto;

import FYP.zecoHelpDesk_backend.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;

    private String fullName;

    private String username;

    private String email;

    private String phone;

    private Role role;

    private String specialization;

    private String zone;

    private Boolean active;

    private String imageUrl;

}