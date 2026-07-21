package FYP.zecoHelpDesk_backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String specialization;

    private String zone;

    @Builder.Default
    private Boolean active = true;

}