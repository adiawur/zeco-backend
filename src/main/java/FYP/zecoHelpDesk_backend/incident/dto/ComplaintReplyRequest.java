package FYP.zecoHelpDesk_backend.incident.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComplaintReplyRequest {

    @NotBlank(message = "Reply is required")
    private String reply;
}