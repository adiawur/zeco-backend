package FYP.zecoHelpDesk_backend.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalIncidents;

    private long reported;

    private long assigned;

    private long inProgress;

    private long completed;

    private long resolved;

    private long closed;

    private long highPriority;

    private long mediumPriority;

    private long lowPriority;

    private long slaBreached;

    private long slaAtRisk;

    private long slaOnTime;

    private double resolutionRate;
}