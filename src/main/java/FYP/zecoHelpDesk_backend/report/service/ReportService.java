package FYP.zecoHelpDesk_backend.report.service;

import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import FYP.zecoHelpDesk_backend.incident.entity.Priority;

import FYP.zecoHelpDesk_backend.report.dto.DashboardResponse;
import FYP.zecoHelpDesk_backend.report.dto.PriorityReportResponse;
import FYP.zecoHelpDesk_backend.report.dto.SlaReportResponse;
import FYP.zecoHelpDesk_backend.report.dto.StatusReportResponse;
import FYP.zecoHelpDesk_backend.report.dto.TypeReportResponse;

import FYP.zecoHelpDesk_backend.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository repository;


    // =========================================================
    // DASHBOARD SUMMARY
    // =========================================================

    public DashboardResponse getDashboard() {

        long total =
                repository.count();


        long reported =
                repository.countByStatus(
                        IncidentStatus.REPORTED
                );


        long assigned =
                repository.countByStatus(
                        IncidentStatus.ASSIGNED
                );


        long inProgress =
                repository.countByStatus(
                        IncidentStatus.IN_PROGRESS
                );


        long completed =
                repository.countByStatus(
                        IncidentStatus.COMPLETED
                );


        long resolved =
                repository.countByStatus(
                        IncidentStatus.RESOLVED
                );


        long closed =
                repository.countByStatus(
                        IncidentStatus.CLOSED
                );


        long high =
                repository.countByPriority(
                        Priority.HIGH
                );


        long medium =
                repository.countByPriority(
                        Priority.MEDIUM
                );


        long low =
                repository.countByPriority(
                        Priority.LOW
                );


        // =====================================================
        // SLA
        // =====================================================

        List<Incident> incidents =
                repository.findAll();


        long slaBreached = 0;

        long slaAtRisk = 0;

        long slaOnTime = 0;


        LocalDateTime now =
                LocalDateTime.now();


        for (Incident incident : incidents) {

            if (incident.getSlaDeadline() == null) {

                continue;
            }


            // Resolved / Closed incidents are no longer
            // considered active SLA incidents.

            if (
                    incident.getStatus()
                            == IncidentStatus.RESOLVED
                            ||
                            incident.getStatus()
                                    == IncidentStatus.CLOSED
            ) {

                continue;
            }


            if (
                    now.isAfter(
                            incident.getSlaDeadline()
                    )
            ) {

                slaBreached++;

                continue;
            }


            long minutesRemaining =
                    Duration.between(
                            now,
                            incident.getSlaDeadline()
                    ).toMinutes();


            if (minutesRemaining <= 30) {

                slaAtRisk++;

            } else {

                slaOnTime++;
            }
        }


        // =====================================================
        // RESOLUTION RATE
        // =====================================================

        double resolutionRate = 0;


        if (total > 0) {

            long resolvedIncidents =
                    resolved + closed;


            resolutionRate =
                    (
                            (double)
                                    resolvedIncidents
                                    / total
                    )
                            * 100;
        }


        return DashboardResponse.builder()

                .totalIncidents(
                        total
                )

                .reported(
                        reported
                )

                .assigned(
                        assigned
                )

                .inProgress(
                        inProgress
                )

                .completed(
                        completed
                )

                .resolved(
                        resolved
                )

                .closed(
                        closed
                )

                .highPriority(
                        high
                )

                .mediumPriority(
                        medium
                )

                .lowPriority(
                        low
                )

                .slaBreached(
                        slaBreached
                )

                .slaAtRisk(
                        slaAtRisk
                )

                .slaOnTime(
                        slaOnTime
                )

                .resolutionRate(
                        Math.round(
                                resolutionRate * 100.0
                        ) / 100.0
                )

                .build();
    }


    // =========================================================
    // STATUS REPORT
    // =========================================================

    public List<StatusReportResponse> getStatusReport() {

        return repository
                .countByStatus()
                .stream()
                .map(row ->

                        StatusReportResponse.builder()

                                .status(
                                        row[0].toString()
                                )

                                .count(
                                        ((Number) row[1])
                                                .longValue()
                                )

                                .build()
                )
                .toList();
    }


    // =========================================================
    // PRIORITY REPORT
    // =========================================================

    public List<PriorityReportResponse>
    getPriorityReport() {

        return repository
                .countByPriority()
                .stream()
                .map(row ->

                        PriorityReportResponse.builder()

                                .priority(
                                        row[0].toString()
                                )

                                .count(
                                        ((Number) row[1])
                                                .longValue()
                                )

                                .build()
                )
                .toList();
    }


    // =========================================================
    // INCIDENT TYPE REPORT
    // =========================================================

    public List<TypeReportResponse>
    getTypeReport() {

        return repository
                .countByIncidentType()
                .stream()
                .map(row ->

                        TypeReportResponse.builder()

                                .incidentType(
                                        row[0].toString()
                                )

                                .count(
                                        ((Number) row[1])
                                                .longValue()
                                )

                                .build()
                )
                .toList();
    }


    // =========================================================
    // SLA REPORT
    // =========================================================

    public List<SlaReportResponse>
    getSlaReport() {

        List<SlaReportResponse> result =
                new ArrayList<>();


        long breached = 0;

        long atRisk = 0;

        long onTime = 0;


        LocalDateTime now =
                LocalDateTime.now();


        List<Incident> incidents =
                repository.findAll();


        for (Incident incident : incidents) {

            if (incident.getSlaDeadline() == null) {

                continue;
            }


            if (
                    incident.getStatus()
                            == IncidentStatus.RESOLVED
                            ||
                            incident.getStatus()
                                    == IncidentStatus.CLOSED
            ) {

                continue;
            }


            if (
                    now.isAfter(
                            incident.getSlaDeadline()
                    )
            ) {

                breached++;

                continue;
            }


            long minutesRemaining =
                    Duration.between(
                            now,
                            incident.getSlaDeadline()
                    ).toMinutes();


            if (minutesRemaining <= 30) {

                atRisk++;

            } else {

                onTime++;
            }
        }


        result.add(
                SlaReportResponse.builder()
                        .slaStatus("BREACHED")
                        .count(breached)
                        .build()
        );


        result.add(
                SlaReportResponse.builder()
                        .slaStatus("AT_RISK")
                        .count(atRisk)
                        .build()
        );


        result.add(
                SlaReportResponse.builder()
                        .slaStatus("ON_TIME")
                        .count(onTime)
                        .build()
        );


        return result;
    }
}