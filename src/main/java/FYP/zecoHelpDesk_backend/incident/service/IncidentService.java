package FYP.zecoHelpDesk_backend.incident.service;

import FYP.zecoHelpDesk_backend.incident.dto.IncidentRequest;
import FYP.zecoHelpDesk_backend.incident.dto.IncidentResponse;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentType;
import FYP.zecoHelpDesk_backend.incident.entity.Priority;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentRepository;
import FYP.zecoHelpDesk_backend.util.LocationUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository repository;

    private final LocationUtil locationUtil;


    // =========================================================
    // REPORT INCIDENT
    // =========================================================

    public IncidentResponse report(
            IncidentRequest request,
            MultipartFile photo
    ) {

        // -----------------------------------------------------
        // PHOTO REQUIREMENT
        // -----------------------------------------------------

        boolean photoRequired =
                requiresPhoto(
                        request.getIncidentType()
                );

        if (
                photoRequired
                        &&
                        (photo == null || photo.isEmpty())
        ) {

            throw new RuntimeException(
                    "Photo evidence is required for this incident type"
            );
        }


        // -----------------------------------------------------
        // REPORT TIME
        // -----------------------------------------------------

        LocalDateTime now =
                LocalDateTime.now();


        // -----------------------------------------------------
        // AUTOMATIC LOCATION
        // -----------------------------------------------------

        String detectedLocation =
                locationUtil.reverseGeocode(
                        request.getLatitude(),
                        request.getLongitude()
                );


        // -----------------------------------------------------
        // AUTOMATIC PRIORITY
        // -----------------------------------------------------

        Priority priority =
                priorityForType(
                        request.getIncidentType()
                );


        // -----------------------------------------------------
        // SLA DEADLINE
        // -----------------------------------------------------

        LocalDateTime slaDeadline =
                calculateSlaDeadline(
                        request.getIncidentType(),
                        now
                );


        // -----------------------------------------------------
        // PHOTO
        // -----------------------------------------------------

        String attachment = null;

        if (
                photo != null
                        &&
                        !photo.isEmpty()
        ) {

            attachment =
                    savePhoto(photo);
        }


        // -----------------------------------------------------
        // CREATE INCIDENT
        // -----------------------------------------------------

        Incident incident =
                Incident.builder()

                        .ticketId(
                                generateTicketId()
                        )

                        .reporterName(
                                request.getReporterName()
                        )

                        .phone(
                                request.getPhone()
                        )

                        .email(
                                request.getEmail()
                        )

                        .incidentType(
                                request.getIncidentType()
                        )

                        .description(
                                request.getDescription()
                        )

                        .location(
                                detectedLocation
                        )

                        .landmark(
                                request.getLandmark()
                        )

                        .latitude(
                                request.getLatitude()
                        )

                        .longitude(
                                request.getLongitude()
                        )

                        .attachment(
                                attachment
                        )

                        .resolutionNotes(
                                null
                        )

                        .priority(
                                priority
                        )

                        .status(
                                IncidentStatus.REPORTED
                        )

                        .reportedAt(
                                now
                        )

                        .updatedAt(
                                now
                        )

                        .slaDeadline(
                                slaDeadline
                        )

                        .slaAlertSent(
                                false
                        )

                        .build();


        Incident savedIncident =
                repository.save(
                        incident
                );


        return toResponse(
                savedIncident
        );
    }


    // =========================================================
    // SLA DEADLINE
    // =========================================================

    private LocalDateTime calculateSlaDeadline(
            IncidentType type,
            LocalDateTime reportedAt
    ) {

        Priority priority =
                priorityForType(
                        type
                );


        /*
         * SRS:
         * High-priority complaints must be addressed
         * within ZECO's maximum response time,
         * e.g. 2 hours.
         *
         * Medium and Low SLA values are not finalized.
         * Therefore no values are invented.
         */

        if (
                priority == Priority.HIGH
        ) {

            return reportedAt.plusHours(2);
        }


        return null;
    }


    // =========================================================
    // SLA STATUS
    // =========================================================

    private String calculateSlaStatus(
            Incident incident
    ) {

        // -----------------------------------------------------
        // NO SLA
        // -----------------------------------------------------

        if (
                incident.getSlaDeadline() == null
        ) {

            return "NOT_APPLICABLE";
        }


        // -----------------------------------------------------
        // COMPLETED / RESOLVED
        // -----------------------------------------------------

        if (
                incident.getStatus() == IncidentStatus.RESOLVED
                        ||
                        incident.getStatus() == IncidentStatus.CLOSED
        ) {

            return "COMPLETED";
        }


        LocalDateTime now =
                LocalDateTime.now();


        LocalDateTime deadline =
                incident.getSlaDeadline();


        // -----------------------------------------------------
        // SLA BREACHED
        // -----------------------------------------------------

        if (
                now.isAfter(deadline)
        ) {

            return "BREACHED";
        }


        // -----------------------------------------------------
        // REMAINING TIME
        // -----------------------------------------------------

        long remainingMinutes =
                Duration.between(
                        now,
                        deadline
                ).toMinutes();


        // -----------------------------------------------------
        // AT RISK
        // -----------------------------------------------------

        if (
                remainingMinutes <= 30
        ) {

            return "AT_RISK";
        }


        // -----------------------------------------------------
        // ON TIME
        // -----------------------------------------------------

        return "ON_TIME";
    }


    // =========================================================
    // ELAPSED MINUTES
    // =========================================================

    private Long calculateElapsedMinutes(
            Incident incident
    ) {

        if (
                incident.getReportedAt() == null
        ) {

            return 0L;
        }


        return Duration.between(
                incident.getReportedAt(),
                LocalDateTime.now()
        ).toMinutes();
    }


    // =========================================================
    // REMAINING MINUTES
    // =========================================================

    private Long calculateRemainingMinutes(
            Incident incident
    ) {

        if (
                incident.getSlaDeadline() == null
        ) {

            return null;
        }


        return Duration.between(
                LocalDateTime.now(),
                incident.getSlaDeadline()
        ).toMinutes();
    }


    // =========================================================
    // GENERATE TICKET ID
    // =========================================================

    private String generateTicketId() {

        String time =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmssSSS"
                                )
                        );


        return "ZHD-" + time;
    }


    // =========================================================
    // AUTOMATIC PRIORITY
    // =========================================================

    private Priority priorityForType(
            IncidentType type
    ) {

        return switch (type) {

            case TRANSFORMER_FAULT,
                 EXPOSED_WIRES,
                 FIRE_HAZARD ->

                    Priority.HIGH;


            case POWER_OUTAGE,
                 BROKEN_POLE,
                 VOLTAGE_FLUCTUATION ->

                    Priority.MEDIUM;


            case METER_ISSUE,
                 BILLING_ISSUE,
                 STREET_LIGHT_FAULT,
                 OTHER ->

                    Priority.LOW;
        };
    }


    // =========================================================
    // RESPONSE MAPPING
    // =========================================================

    public IncidentResponse toResponse(
            Incident incident
    ) {

        return IncidentResponse.builder()

                .id(
                        incident.getId()
                )

                .ticketId(
                        incident.getTicketId()
                )

                .reporterName(
                        incident.getReporterName()
                )

                .phone(
                        incident.getPhone()
                )

                .email(
                        incident.getEmail()
                )

                .incidentType(
                        incident.getIncidentType()
                )

                .description(
                        incident.getDescription()
                )

                .location(
                        incident.getLocation()
                )

                .landmark(
                        incident.getLandmark()
                )

                .latitude(
                        incident.getLatitude()
                )

                .longitude(
                        incident.getLongitude()
                )

                .attachment(
                        incident.getAttachment()
                )

                // -------------------------------------------------
                // TECHNICIAN COMPLETION NOTES
                // -------------------------------------------------

                .resolutionNotes(
                        incident.getResolutionNotes()
                )

                .priority(
                        incident.getPriority()
                )

                .status(
                        incident.getStatus()
                )

                .reportedAt(
                        incident.getReportedAt()
                )

                .updatedAt(
                        incident.getUpdatedAt()
                )

                // -------------------------------------------------
                // SLA
                // -------------------------------------------------

                .slaDeadline(
                        incident.getSlaDeadline()
                )

                .slaStatus(
                        calculateSlaStatus(
                                incident
                        )
                )

                .elapsedMinutes(
                        calculateElapsedMinutes(
                                incident
                        )
                )

                .remainingMinutes(
                        calculateRemainingMinutes(
                                incident
                        )
                )

                .slaAlertSent(
                        incident.getSlaAlertSent()
                )

                .build();
    }


    // =========================================================
    // PHOTO REQUIREMENT
    // =========================================================

    private boolean requiresPhoto(
            IncidentType type
    ) {

        return switch (type) {

            case TRANSFORMER_FAULT,
                 BROKEN_POLE,
                 EXPOSED_WIRES,
                 FIRE_HAZARD ->

                    true;


            default ->

                    false;
        };
    }


    // =========================================================
    // SAVE INCIDENT PHOTO
    // =========================================================

    private String savePhoto(
            MultipartFile photo
    ) {

        try {

            // -------------------------------------------------
            // UPLOAD DIRECTORY
            // -------------------------------------------------

            java.nio.file.Path uploadDirectory =
                    java.nio.file.Paths
                            .get(
                                    System.getProperty("user.dir"),
                                    "uploads",
                                    "incidents"
                            )
                            .toAbsolutePath()
                            .normalize();


            // -------------------------------------------------
            // CREATE DIRECTORY
            // -------------------------------------------------

            java.nio.file.Files.createDirectories(
                    uploadDirectory
            );


            // -------------------------------------------------
            // ORIGINAL FILE NAME
            // -------------------------------------------------

            String originalName =
                    photo.getOriginalFilename();


            if (
                    originalName == null
                            ||
                            originalName.isBlank()
            ) {

                originalName =
                        "incident-photo";
            }


            // -------------------------------------------------
            // REMOVE UNSAFE PATH
            // -------------------------------------------------

            originalName =
                    new File(
                            originalName
                    ).getName();


            // -------------------------------------------------
            // UNIQUE FILE NAME
            // -------------------------------------------------

            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + originalName;


            // -------------------------------------------------
            // FINAL PATH
            // -------------------------------------------------

            java.nio.file.Path targetPath =
                    uploadDirectory
                            .resolve(
                                    fileName
                            )
                            .normalize();


            // -------------------------------------------------
            // COPY FILE
            // -------------------------------------------------

            try (
                    java.io.InputStream inputStream =
                            photo.getInputStream()
            ) {

                java.nio.file.Files.copy(
                        inputStream,
                        targetPath,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }


            // -------------------------------------------------
            // RETURN FILE NAME
            // -------------------------------------------------

            return fileName;


        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to save incident photo: "
                            + e.getMessage()
            );
        }
    }
}