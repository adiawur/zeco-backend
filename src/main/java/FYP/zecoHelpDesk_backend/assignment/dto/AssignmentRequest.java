package FYP.zecoHelpDesk_backend.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {

    @NotNull
    private Long incidentId;

    @NotNull
    private Long technicianId;
}