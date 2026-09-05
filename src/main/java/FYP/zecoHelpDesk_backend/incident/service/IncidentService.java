package FYP.zecoHelpDesk_backend.incident.service;

import FYP.zecoHelpDesk_backend.assignment.entity.Assignment;
import FYP.zecoHelpDesk_backend.assignment.repository.AssignmentRepository;
import FYP.zecoHelpDesk_backend.incident.dto.IncidentRequest;
import FYP.zecoHelpDesk_backend.incident.dto.IncidentResponse;
import FYP.zecoHelpDesk_backend.incident.dto.TrackIncidentRequest;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentType;
import FYP.zecoHelpDesk_backend.incident.entity.Priority;
import org.springframework.security.core.Authentication;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentRepository;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;
import FYP.zecoHelpDesk_backend.util.LocationUtil;
import FYP.zecoHelpDesk_backend.util.Zone;
import FYP.zecoHelpDesk_backend.util.ZoneUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository repository;

    private final LocationUtil locationUtil;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;


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
        // AUTOMATIC ZONE
        // -----------------------------------------------------
        //
        // Zone is determined automatically from the
        // latitude and longitude supplied by the system.
        //
        // The citizen does not enter the zone manually.
        // -----------------------------------------------------

        Zone zone =
                ZoneUtils.getZoneByCoordinates(
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
                                request.getReporterName().trim()
                        )

                        .phone(
                                request.getPhone().trim()
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

                        .zone(
                                zone
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
                priorityForType(type);

        /*
         * Current SRS logic:
         *
         * HIGH = 2 hours
         *
         * Medium and Low remain without
         * a fixed SLA because their values
         * have not been finalized.
         */

        if (priority == Priority.HIGH) {

            return reportedAt.plusHours(2);
        }

        return null;
    }


    // =========================================================
    // SLA STATUS
    // =========================================================
    //
    // SLA states:
    //
    // NOT_APPLICABLE
    //      -> Incident has no SLA deadline
    //
    // COMPLETED
    //      -> Incident has been completed/resolved/closed
    //
    // BREACHED
    //      -> SLA deadline has passed while incident is still active
    //
    // AT_RISK
    //      -> 30 minutes or less remaining
    //
    // ON_TIME
    //      -> SLA is still within allowed time
    //
    // =========================================================

    private String calculateSlaStatus(
            Incident incident
    ) {

        // -----------------------------------------------------
        // NO SLA
        // -----------------------------------------------------

        if (incident == null) {

            return "NOT_APPLICABLE";
        }

        if (incident.getSlaDeadline() == null) {

            return "NOT_APPLICABLE";
        }


        // -----------------------------------------------------
        // FINAL / COMPLETED STATUSES
        // -----------------------------------------------------
        //
        // COMPLETED:
        // Technician has finished the work.
        //
        // RESOLVED:
        // Supervisor has verified/resolved the incident.
        //
        // CLOSED:
        // Incident is fully closed.
        //
        // These statuses should not continue showing
        // BREACHED or AT_RISK after the work is finished.
        // -----------------------------------------------------

        if (
                incident.getStatus() == IncidentStatus.COMPLETED
                        ||
                        incident.getStatus() == IncidentStatus.RESOLVED
                        ||
                        incident.getStatus() == IncidentStatus.CLOSED
        ) {

            return "COMPLETED";
        }


        // -----------------------------------------------------
        // CURRENT TIME
        // -----------------------------------------------------

        LocalDateTime now =
                LocalDateTime.now();


        LocalDateTime deadline =
                incident.getSlaDeadline();


        // -----------------------------------------------------
        // SLA BREACHED
        // -----------------------------------------------------
        //
        // If the deadline has already passed and the incident
        // is still active, the SLA is breached.
        // -----------------------------------------------------

        if (now.isAfter(deadline)) {

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
        // SLA AT RISK
        // -----------------------------------------------------
        //
        // 30 minutes or less remaining.
        // -----------------------------------------------------

        if (remainingMinutes <= 30) {

            return "AT_RISK";
        }


        // -----------------------------------------------------
        // SLA ON TIME
        // -----------------------------------------------------

        return "ON_TIME";
    }


    // =========================================================
    // ELAPSED MINUTES
    // =========================================================

    private Long calculateElapsedMinutes(
            Incident incident
    ) {

        if (incident.getReportedAt() == null) {

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

        if (incident.getSlaDeadline() == null) {

            return null;
        }


        return Duration.between(
                LocalDateTime.now(),
                incident.getSlaDeadline()
        ).toMinutes();
    }


    // =========================================================
    // PUBLIC INCIDENT TRACKING
    // NO LOGIN REQUIRED
    //
    // fullName + phone = required identity
    // email = optional additional verification
    // =========================================================

    public List<IncidentResponse> trackIncidents(
            TrackIncidentRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Tracking information is required"
            );
        }


        String fullName =
                request.getFullName() == null
                        ? ""
                        : request.getFullName().trim();

        String phone =
                request.getPhone() == null
                        ? ""
                        : request.getPhone().trim();

        String email =
                request.getEmail() == null
                        ? ""
                        : request.getEmail().trim();


        // -----------------------------------------------------
        // REQUIRED FIELDS
        // -----------------------------------------------------

        if (fullName.isBlank()) {

            throw new RuntimeException(
                    "Full name is required"
            );
        }


        if (phone.isBlank()) {

            throw new RuntimeException(
                    "Phone number is required"
            );
        }


        List<Incident> incidents;


        // -----------------------------------------------------
        // EMAIL PROVIDED
        // -----------------------------------------------------

        if (!email.isBlank()) {

            incidents =
                    repository
                            .findByReporterNameIgnoreCaseAndPhoneAndEmail(
                                    fullName,
                                    phone,
                                    email
                            );

        }

        // -----------------------------------------------------
        // EMAIL NOT PROVIDED
        // -----------------------------------------------------

        else {

            incidents =
                    repository
                            .findByReporterNameIgnoreCaseAndPhone(
                                    fullName,
                                    phone
                            );
        }


        // -----------------------------------------------------
        // RETURN ALL MATCHING INCIDENTS
        // -----------------------------------------------------

        return incidents
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET INCIDENT ZONE
    //
    // Zone is already stored in the incident record.
    // It is assigned automatically when the incident
    // is reported using latitude and longitude.
    // =========================================================

    public Zone getIncidentZone(
            Incident incident
    ) {

        if (incident == null) {

            throw new RuntimeException(
                    "Incident is required"
            );
        }


        // -----------------------------------------------------
        // READ STORED ZONE
        // -----------------------------------------------------

        if (incident.getZone() == null) {

            throw new RuntimeException(
                    "Incident has no assigned zone"
            );
        }


        return incident.getZone();
    }


    // =========================================================
    // GET INCIDENT ZONE BY ID
    // =========================================================

    public Zone getIncidentZoneById(
            Long incidentId
    ) {

        Incident incident =
                repository.findById(
                        incidentId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Incident not found"
                        )
                );


        return getIncidentZone(
                incident
        );
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

                .complaintAllowed(
                        canSubmitComplaint(incident)
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

            java.nio.file.Path uploadDirectory =
                    java.nio.file.Paths
                            .get(
                                    System.getProperty("user.dir"),
                                    "uploads",
                                    "incidents"
                            )
                            .toAbsolutePath()
                            .normalize();


            java.nio.file.Files.createDirectories(
                    uploadDirectory
            );


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


            originalName =
                    new File(
                            originalName
                    ).getName();


            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + originalName;


            java.nio.file.Path targetPath =
                    uploadDirectory
                            .resolve(
                                    fileName
                            )
                            .normalize();


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


            return fileName;


        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to save incident photo: "
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // CUSTOMER COMPLAINT / FEEDBACK ELIGIBILITY
    //
    // Customer can complain when:
    // 1. Incident has been assigned to technician
    // 2. Assignment delay has passed
    // 3. Incident is not completed/resolved/closed
    // =========================================================

    public boolean canSubmitComplaint(
            Incident incident
    ) {

        if (incident == null) {
            return false;
        }

        // -----------------------------------------------------
        // FINAL STATUSES
        // -----------------------------------------------------

        if (
                incident.getStatus() == IncidentStatus.COMPLETED
                        ||
                        incident.getStatus() == IncidentStatus.RESOLVED
                        ||
                        incident.getStatus() == IncidentStatus.CLOSED
        ) {

            return false;
        }


        // -----------------------------------------------------
        // FIND ASSIGNMENT
        // -----------------------------------------------------

        Assignment assignment =
                assignmentRepository
                        .findByIncident(incident)
                        .orElse(null);


        // -----------------------------------------------------
        // NOT ASSIGNED YET
        // -----------------------------------------------------

        if (
                assignment == null
                        ||
                        assignment.getAssignedAt() == null
        ) {

            return false;
        }


        // -----------------------------------------------------
        // ASSIGNMENT DELAY
        // -----------------------------------------------------
        //
        // Customer can complain after 30 minutes.
        //
        // You can later move this to application.properties.
        // -----------------------------------------------------

        LocalDateTime complaintTime =
                assignment
                        .getAssignedAt()
                        .plusMinutes(30);


        return LocalDateTime.now()
                .isAfter(complaintTime);
    }


    // =========================================================
    // SUPERVISOR
    // GET INCIDENTS FROM MY ZONE ONLY
    // =========================================================

    public List<IncidentResponse> getSupervisorZoneIncidents(
            Authentication authentication
    ) {

        User supervisor =
                userRepository
                        .findByUsername(authentication.getName())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supervisor not found"
                                )
                        );

        // -----------------------------------------------------
        // CHECK ROLE
        // -----------------------------------------------------

        if (
                supervisor.getRole() == null ||
                        !supervisor.getRole()
                                .name()
                                .equals("SUPERVISOR")
        ) {

            throw new RuntimeException(
                    "Only supervisors can access this resource"
            );
        }

        // -----------------------------------------------------
        // CHECK ZONE
        // -----------------------------------------------------

        if (
                supervisor.getZone() == null ||
                        supervisor.getZone().isBlank()
        ) {

            throw new RuntimeException(
                    "Supervisor has no assigned zone"
            );
        }

        String supervisorZone =
                supervisor.getZone();

        // -----------------------------------------------------
        // FILTER INCIDENTS BY ZONE
        // -----------------------------------------------------

        return repository
                .findAll()
                .stream()
                .filter(incident -> {

                    Zone incidentZone =
                            getIncidentZone(incident);

                    return incidentZone.name()
                            .equalsIgnoreCase(
                                    supervisorZone
                            );
                })
                .map(this::toResponse)
                .toList();
    }
}