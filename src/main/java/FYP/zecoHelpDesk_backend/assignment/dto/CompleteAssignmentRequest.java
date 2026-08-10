package FYP.zecoHelpDesk_backend.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteAssignmentRequest {

    @NotBlank(message = "Completion notes are required")
    private String notes;
}