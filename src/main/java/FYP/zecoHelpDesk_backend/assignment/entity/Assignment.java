package FYP.zecoHelpDesk_backend.assignment.entity;

import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Incident being assigned
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "incident_id",
            nullable = false
    )
    private Incident incident;

    // Technician assigned
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "technician_id",
            nullable = false
    )
    private User technician;

    // When supervisor/authorized staff assigned it
    @Column(nullable = false)
    private LocalDateTime assignedAt;

    // Filled when technician completes the assignment
    private LocalDateTime completedAt;
}