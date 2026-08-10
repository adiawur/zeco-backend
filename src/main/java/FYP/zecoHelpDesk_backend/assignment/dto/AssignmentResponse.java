package FYP.zecoHelpDesk_backend.assignment.dto;

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
}