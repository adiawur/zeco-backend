package FYP.zecoHelpDesk_backend.assignment.service;

import FYP.zecoHelpDesk_backend.assignment.dto.AssignmentRequest;
import FYP.zecoHelpDesk_backend.assignment.dto.AssignmentResponse;
import FYP.zecoHelpDesk_backend.assignment.entity.Assignment;
import FYP.zecoHelpDesk_backend.assignment.repository.AssignmentRepository;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentRepository;
import FYP.zecoHelpDesk_backend.notification.service.EmailNotificationService;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;
import FYP.zecoHelpDesk_backend.assignment.dto.CompleteAssignmentRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    private final IncidentRepository incidentRepository;

    private final UserRepository userRepository;

    private final EmailNotificationService emailNotificationService;


    // =========================================================
    // ASSIGN INCIDENT TO TECHNICIAN
    // =========================================================

    public AssignmentResponse assign(
            AssignmentRequest request
    ) {

        // -----------------------------------------------------
        // FIND INCIDENT
        // -----------------------------------------------------

        Incident incident =
                incidentRepository.findById(
                        request.getIncidentId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Incident not found"
                        )
                );


        // -----------------------------------------------------
        // FIND TECHNICIAN
        // -----------------------------------------------------

        User technician =
                userRepository.findById(
                        request.getTechnicianId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Technician not found"
                        )
                );


        // -----------------------------------------------------
        // CHECK TECHNICIAN ROLE
        // -----------------------------------------------------

        if (
                technician.getRole() == null
                        ||
                        !technician.getRole()
                                .name()
                                .equals("TECHNICIAN")
        ) {

            throw new RuntimeException(
                    "Selected user is not a technician"
            );
        }


        // -----------------------------------------------------
        // CHECK ACTIVE STATUS
        // -----------------------------------------------------

        if (!technician.getActive()) {

            throw new RuntimeException(
                    "Selected technician is inactive"
            );
        }


        // -----------------------------------------------------
        // CHECK EXISTING ASSIGNMENT
        // -----------------------------------------------------

        if (
                assignmentRepository
                        .findByIncident(incident)
                        .isPresent()
        ) {

            throw new RuntimeException(
                    "This incident is already assigned"
            );
        }


        // -----------------------------------------------------
        // CREATE ASSIGNMENT
        // -----------------------------------------------------

        Assignment assignment =
                Assignment.builder()

                        .incident(incident)

                        .technician(technician)

                        .assignedAt(
                                LocalDateTime.now()
                        )

                        .build();


        Assignment savedAssignment =
                assignmentRepository.save(
                        assignment
                );


        // -----------------------------------------------------
        // UPDATE INCIDENT STATUS
        // -----------------------------------------------------

        incident.setStatus(
                IncidentStatus.ASSIGNED
        );

        incident.setUpdatedAt(
                LocalDateTime.now()
        );

        incidentRepository.save(
                incident
        );


        // -----------------------------------------------------
        // NOTIFY TECHNICIAN
        // -----------------------------------------------------

        if (
                technician.getEmail() != null
                        &&
                        !technician.getEmail().isBlank()
        ) {

            emailNotificationService
                    .sendAssignmentNotification(

                            technician.getEmail(),

                            technician.getFullName(),

                            incident.getTicketId(),

                            incident.getIncidentType().name(),

                            incident.getPriority().name(),

                            incident.getLocation()
                    );
        }


        return toResponse(
                savedAssignment
        );
    }


    // =========================================================
    // GET ALL ASSIGNMENTS
    // =========================================================

    public List<AssignmentResponse> getAll() {

        return assignmentRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // GET ASSIGNMENT BY ID
    // =========================================================

    public AssignmentResponse getById(
            Long id
    ) {

        Assignment assignment =
                assignmentRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                )
                        );

        return toResponse(
                assignment
        );
    }


    // =========================================================
    // GET TECHNICIAN ASSIGNMENTS
    // =========================================================

    public List<AssignmentResponse> getByTechnician(
            Long technicianId
    ) {

        User technician =
                userRepository.findById(
                        technicianId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Technician not found"
                        )
                );


        return assignmentRepository
                .findByTechnician(technician)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // =========================================================
    // RESPONSE MAPPER
    // =========================================================

    public AssignmentResponse toResponse(
            Assignment assignment
    ) {

        Incident incident =
                assignment.getIncident();

        User technician =
                assignment.getTechnician();


        return AssignmentResponse.builder()

                .id(
                        assignment.getId()
                )

                .incidentId(
                        incident.getId()
                )

                .ticketId(
                        incident.getTicketId()
                )

                .technicianId(
                        technician.getId()
                )

                .technicianName(
                        technician.getFullName()
                )

                .specialization(
                        technician.getSpecialization()
                )

                .assignedAt(
                        assignment.getAssignedAt()
                )

                .completedAt(
                        assignment.getCompletedAt()
                )

                .build();
    }

    // =========================================================
// TECHNICIAN START WORK
// =========================================================

    public AssignmentResponse startWork(
            Long assignmentId,
            Authentication authentication
    ) {

        Assignment assignment =
                assignmentRepository.findById(
                        assignmentId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Assignment not found"
                        )
                );


        User technician =
                assignment.getTechnician();


        // -----------------------------------------------------
        // CHECK LOGGED-IN TECHNICIAN
        // -----------------------------------------------------

        if (
                !technician.getUsername()
                        .equals(authentication.getName())
        ) {

            throw new RuntimeException(
                    "You are not allowed to update this assignment"
            );
        }


        Incident incident =
                assignment.getIncident();


        // -----------------------------------------------------
        // CHECK CURRENT STATUS
        // -----------------------------------------------------

        if (
                incident.getStatus()
                        != IncidentStatus.ASSIGNED
        ) {

            throw new RuntimeException(
                    "Incident is not ready to start"
            );
        }


        // -----------------------------------------------------
        // UPDATE STATUS
        // -----------------------------------------------------

        incident.setStatus(
                IncidentStatus.IN_PROGRESS
        );

        incident.setUpdatedAt(
                LocalDateTime.now()
        );

        incidentRepository.save(
                incident
        );


        // -----------------------------------------------------
        // CUSTOMER EMAIL
        // -----------------------------------------------------

        if (
                incident.getEmail() != null
                        &&
                        !incident.getEmail().isBlank()
        ) {

            emailNotificationService
                    .sendStatusNotification(

                            incident.getEmail(),

                            incident.getTicketId(),

                            IncidentStatus.IN_PROGRESS.name()
                    );
        }


        return toResponse(
                assignment
        );
    }

    // =========================================================
// TECHNICIAN COMPLETE WORK
// =========================================================

    public AssignmentResponse completeWork(

            Long assignmentId,

            CompleteAssignmentRequest request,

            MultipartFile photo,

            Authentication authentication

    ) {

        Assignment assignment =
                assignmentRepository.findById(
                        assignmentId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Assignment not found"
                        )
                );


        User technician =
                assignment.getTechnician();


        // -----------------------------------------------------
        // CHECK LOGGED-IN TECHNICIAN
        // -----------------------------------------------------

        if (
                !technician.getUsername()
                        .equals(authentication.getName())
        ) {

            throw new RuntimeException(
                    "You are not allowed to update this assignment"
            );
        }


        Incident incident =
                assignment.getIncident();


        // -----------------------------------------------------
        // CHECK STATUS
        // -----------------------------------------------------

        if (
                incident.getStatus()
                        != IncidentStatus.IN_PROGRESS
        ) {

            throw new RuntimeException(
                    "Incident must be in progress before completion"
            );
        }


        // -----------------------------------------------------
        // PHOTO REQUIRED
        // -----------------------------------------------------

        // =========================================================
// SAVE COMPLETION PHOTO IF PROVIDED
// =========================================================

        String attachment =
                incident.getAttachment();

        if (
                photo != null
                        &&
                        !photo.isEmpty()
        ) {

            attachment =
                    saveCompletionPhoto(
                            photo
                    );
        }

        // -----------------------------------------------------
        // UPDATE INCIDENT
        // -----------------------------------------------------

        // =========================================================
// UPDATE INCIDENT
// =========================================================

        incident.setAttachment(
                attachment
        );

        incident.setResolutionNotes(
                request.getNotes()
        );

        incident.setStatus(
                IncidentStatus.COMPLETED
        );

        incident.setUpdatedAt(
                LocalDateTime.now()
        );

        incidentRepository.save(
                incident
        );


        // -----------------------------------------------------
        // UPDATE ASSIGNMENT
        // -----------------------------------------------------

        assignment.setCompletedAt(
                LocalDateTime.now()
        );


        Assignment savedAssignment =
                assignmentRepository.save(
                        assignment
                );


        // -----------------------------------------------------
        // CUSTOMER NOTIFICATION
        // -----------------------------------------------------

        if (
                incident.getEmail() != null
                        &&
                        !incident.getEmail().isBlank()
        ) {

            emailNotificationService
                    .sendStatusNotification(

                            incident.getEmail(),

                            incident.getTicketId(),

                            IncidentStatus.COMPLETED.name()
                    );
        }


        return toResponse(
                savedAssignment
        );
    }

    // =========================================================
// SAVE COMPLETION PHOTO
// =========================================================

    private String saveCompletionPhoto(
            MultipartFile photo
    ) {

        try {

            String uploadDirectory =
                    "uploads/incidents/completion/";

            File directory =
                    new File(
                            uploadDirectory
                    );


            if (!directory.exists()) {

                directory.mkdirs();
            }


            String originalName =
                    photo.getOriginalFilename();


            if (
                    originalName == null
                            ||
                            originalName.isBlank()
            ) {

                originalName =
                        "completion-photo";
            }


            originalName =
                    new File(
                            originalName
                    ).getName();


            String fileName =
                    System.currentTimeMillis()
                            + "_"
                            + originalName;


            File file =
                    new File(
                            uploadDirectory
                                    + fileName
                    );


            photo.transferTo(
                    file
            );


            return fileName;


        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to save completion photo"
            );
        }
    }

    // =========================================================
// SUPERVISOR RESOLVE INCIDENT
// =========================================================

    public AssignmentResponse resolve(
            Long assignmentId
    ) {

        Assignment assignment =
                assignmentRepository.findById(
                        assignmentId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Assignment not found"
                        )
                );


        Incident incident =
                assignment.getIncident();


        if (
                incident.getStatus()
                        != IncidentStatus.COMPLETED
        ) {

            throw new RuntimeException(
                    "Incident must be completed before resolution"
            );
        }


        incident.setStatus(
                IncidentStatus.RESOLVED
        );

        incident.setUpdatedAt(
                LocalDateTime.now()
        );


        incidentRepository.save(
                incident
        );


        // -----------------------------------------------------
        // CUSTOMER NOTIFICATION
        // -----------------------------------------------------

        if (
                incident.getEmail() != null
                        &&
                        !incident.getEmail().isBlank()
        ) {

            emailNotificationService
                    .sendStatusNotification(

                            incident.getEmail(),

                            incident.getTicketId(),

                            IncidentStatus.RESOLVED.name()
                    );
        }


        return toResponse(
                assignment
        );
    }

    // =========================================================
// GET LOGGED-IN TECHNICIAN ASSIGNMENTS
// =========================================================

    public List<AssignmentResponse> getByTechnicianUsername(
            String username
    ) {

        User technician =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Technician not found"
                                )
                        );


        return assignmentRepository
                .findByTechnician(technician)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
// TECHNICIAN
// GET MY ASSIGNMENT BY ID
// =========================================================

    public AssignmentResponse getMyAssignmentById(

            Long id,

            Authentication authentication

    ) {

        Assignment assignment =
                assignmentRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Assignment not found"
                                )
                        );


        User technician =
                assignment.getTechnician();


        if (
                !technician
                        .getUsername()
                        .equals(
                                authentication.getName()
                        )
        ) {

            throw new RuntimeException(
                    "You are not allowed to view this assignment"
            );
        }


        return toResponse(
                assignment
        );
    }
}