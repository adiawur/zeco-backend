package FYP.zecoHelpDesk_backend.incident.dto;

import FYP.zecoHelpDesk_backend.incident.entity.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncidentRequest {

    @NotBlank
    private String reporterName;

    @NotBlank
    private String phone;

    private String email;

    @NotNull
    private IncidentType incidentType;

    @NotBlank
    private String description;

    private String landmark;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

}