package FYP.zecoHelpDesk_backend.incident.dto;

import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentType;
import FYP.zecoHelpDesk_backend.incident.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private Long id;

    private String ticketId;

    private String reporterName;

    private String phone;

    private String email;

    private IncidentType incidentType;

    private String description;

    private String location;

    private String landmark;

    private Double latitude;

    private Double longitude;

    private String attachment;

    private Priority priority;

    private IncidentStatus status;

    private LocalDateTime reportedAt;

    private LocalDateTime updatedAt;

    private LocalDateTime slaDeadline;

    /*
     * SLA information calculated by the system.
     *
     * ON_TIME
     * AT_RISK
     * BREACHED
     * NOT_APPLICABLE
     */
    private String slaStatus;

    /*
     * Minutes elapsed since incident was reported.
     */
    private Long elapsedMinutes;

    /*
     * Minutes remaining before SLA deadline.
     *
     * Negative value means SLA has already been breached.
     */
    private Long remainingMinutes;

    private String resolutionNotes;

    private Boolean slaAlertSent;
}