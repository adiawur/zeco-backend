package FYP.zecoHelpDesk_backend.assignment.dto;

import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;

    private Long incidentId;

    private String ticketId;

    private Long technicianId;

    private String technicianName;

    private String specialization;

    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;

    // =====================================================
    // INCIDENT INFORMATION
    // =====================================================

    private String incidentStatus;

    private String priority;

    private String incidentType;

    private String location;

    // =====================================================
    // SLA
    // =====================================================

    private LocalDateTime slaDeadline;

    private String slaStatus;

    private Long remainingMinutes;

    private Long elapsedMinutes;

    // =====================================================
    // ZONE
    // =====================================================

    private String zone;

    private IncidentStatus status;
}