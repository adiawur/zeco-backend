package FYP.zecoHelpDesk_backend.incident.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_complaints")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "incident_id",
            nullable = false
    )
    private Incident incident;


    @Column(nullable = false)
    private String fullName;


    @Column(nullable = false)
    private String phone;


    private String email;


    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String message;


    @Column(nullable = false)
    private LocalDateTime submittedAt;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;

    private String reply;

    private LocalDateTime repliedAt;

    private String repliedBy;
}