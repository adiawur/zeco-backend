package FYP.zecoHelpDesk_backend.incident.service;

import FYP.zecoHelpDesk_backend.incident.dto.ComplaintReplyRequest;
import FYP.zecoHelpDesk_backend.incident.dto.IncidentComplaintRequest;
import FYP.zecoHelpDesk_backend.incident.entity.ComplaintStatus;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentComplaint;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentComplaintRepository;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentRepository;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;
import FYP.zecoHelpDesk_backend.util.Zone;
import FYP.zecoHelpDesk_backend.util.ZoneUtils;

import org.springframework.security.core.Authentication;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentComplaintService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final IncidentComplaintRepository complaintRepository;

    // =========================================================
// SUBMIT CUSTOMER COMPLAINT / FEEDBACK
// =========================================================

    public IncidentComplaint submit(
            IncidentComplaintRequest request
    ) {

        // -----------------------------------------------------
        // VALIDATE REQUEST
        // -----------------------------------------------------

        if (request == null) {

            throw new RuntimeException(
                    "Complaint information is required"
            );
        }


        // -----------------------------------------------------
        // VALIDATE REQUIRED FIELDS
        // -----------------------------------------------------

        if (
                request.getTicketId() == null
                        ||
                        request.getTicketId().trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Ticket ID is required"
            );
        }


        if (
                request.getFullName() == null
                        ||
                        request.getFullName().trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Full name is required"
            );
        }


        if (
                request.getPhone() == null
                        ||
                        request.getPhone().trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Phone number is required"
            );
        }


        if (
                request.getMessage() == null
                        ||
                        request.getMessage().trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Complaint or feedback message is required"
            );
        }


        // -----------------------------------------------------
        // FIND INCIDENT
        // -----------------------------------------------------

        Incident incident =
                incidentRepository
                        .findByTicketId(
                                request.getTicketId().trim()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Incident not found"
                                )
                        );


        // -----------------------------------------------------
        // VERIFY CUSTOMER NAME
        // -----------------------------------------------------

        if (
                incident.getReporterName() == null
                        ||
                        !incident.getReporterName()
                                .equalsIgnoreCase(
                                        request.getFullName().trim()
                                )
        ) {

            throw new RuntimeException(
                    "The provided information does not match this incident"
            );
        }


        // -----------------------------------------------------
        // VERIFY CUSTOMER PHONE
        // -----------------------------------------------------

        if (
                incident.getPhone() == null
                        ||
                        !incident.getPhone()
                                .equals(
                                        request.getPhone().trim()
                                )
        ) {

            throw new RuntimeException(
                    "The provided information does not match this incident"
            );
        }


        // -----------------------------------------------------
        // CREATE COMPLAINT / FEEDBACK
        //
        // All incident statuses are allowed.
        //
        // Active incident:
        //     Complaint
        //
        // Completed / Resolved / Closed:
        //     Feedback
        // -----------------------------------------------------

        IncidentComplaint complaint =
                IncidentComplaint.builder()

                        .incident(
                                incident
                        )

                        .fullName(
                                request
                                        .getFullName()
                                        .trim()
                        )

                        .phone(
                                request
                                        .getPhone()
                                        .trim()
                        )

                        .email(
                                request.getEmail() != null
                                        ? request.getEmail().trim()
                                        : null
                        )

                        .message(
                                request
                                        .getMessage()
                                        .trim()
                        )

                        .submittedAt(
                                LocalDateTime.now()
                        )

                        .status(
                                ComplaintStatus.SUBMITTED
                        )

                        .build();


        // -----------------------------------------------------
        // SAVE COMPLAINT / FEEDBACK
        // -----------------------------------------------------

        return complaintRepository.save(
                complaint
        );
    }

    // =========================================================
// SUPERVISOR
// GET COMPLAINTS / FEEDBACK FOR MY ZONE
// =========================================================

    public List<IncidentComplaint> getSupervisorComplaints(
            Authentication authentication
    ) {

        User supervisor =
                userRepository
                        .findByUsername(
                                authentication.getName()
                        )
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
                    "Only supervisors can access complaints"
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


        // -----------------------------------------------------
        // GET ALL COMPLAINTS
        // -----------------------------------------------------

        return complaintRepository
                .findAll()
                .stream()
                .filter(complaint -> {

                    Incident incident =
                            complaint.getIncident();

                    Zone incidentZone =
                            getIncidentZone(
                                    incident
                            );

                    return incidentZone.name()
                            .equalsIgnoreCase(
                                    supervisor.getZone()
                            );
                })
                .toList();
    }


// =========================================================
// TECHNICIAN
// GET COMPLAINTS / FEEDBACK FOR MY ZONE
// =========================================================

    public List<IncidentComplaint> getTechnicianComplaints(
            Authentication authentication
    ) {

        User technician =
                userRepository
                        .findByUsername(
                                authentication.getName()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Technician not found"
                                )
                        );


        // -----------------------------------------------------
        // CHECK ROLE
        // -----------------------------------------------------

        if (
                technician.getRole() == null ||
                        !technician.getRole()
                                .name()
                                .equals("TECHNICIAN")
        ) {

            throw new RuntimeException(
                    "Only technicians can access complaints"
            );
        }


        // -----------------------------------------------------
        // CHECK ZONE
        // -----------------------------------------------------

        if (
                technician.getZone() == null ||
                        technician.getZone().isBlank()
        ) {

            throw new RuntimeException(
                    "Technician has no assigned zone"
            );
        }


        // -----------------------------------------------------
        // GET ALL COMPLAINTS
        // -----------------------------------------------------

        return complaintRepository
                .findAll()
                .stream()
                .filter(complaint -> {

                    Incident incident =
                            complaint.getIncident();

                    Zone incidentZone =
                            getIncidentZone(
                                    incident
                            );

                    return incidentZone.name()
                            .equalsIgnoreCase(
                                    technician.getZone()
                            );
                })
                .toList();
    }


// =========================================================
// GET INCIDENT ZONE
// =========================================================

    private Zone getIncidentZone(
            Incident incident
    ) {

        if (
                incident == null
        ) {

            return Zone.ZANZIBAR;
        }


        if (
                incident.getLatitude() == null ||
                        incident.getLongitude() == null
        ) {

            return Zone.ZANZIBAR;
        }


        return ZoneUtils.getZoneByCoordinates(
                incident.getLatitude(),
                incident.getLongitude()
        );
    }

    // =========================================================
// SUPERVISOR
// REPLY TO CUSTOMER COMPLAINT / FEEDBACK
// =========================================================

    public IncidentComplaint replyToComplaint(

            Long complaintId,

            ComplaintReplyRequest request,

            Authentication authentication

    ) {

        // =====================================================
        // GET SUPERVISOR
        // =====================================================

        User supervisor =
                userRepository
                        .findByUsername(
                                authentication.getName()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Supervisor not found"
                                )
                        );


        // =====================================================
        // CHECK ROLE
        // =====================================================

        if (
                supervisor.getRole() == null
                        ||
                        !supervisor.getRole()
                                .name()
                                .equals("SUPERVISOR")
        ) {

            throw new RuntimeException(
                    "Only supervisors can reply to complaints"
            );
        }


        // =====================================================
        // CHECK ZONE
        // =====================================================

        if (
                supervisor.getZone() == null
                        ||
                        supervisor.getZone().isBlank()
        ) {

            throw new RuntimeException(
                    "Supervisor has no assigned zone"
            );
        }


        // =====================================================
        // VALIDATE REPLY
        // =====================================================

        if (
                request == null
                        ||
                        request.getReply() == null
                        ||
                        request.getReply().trim().isEmpty()
        ) {

            throw new RuntimeException(
                    "Reply is required"
            );
        }


        // =====================================================
        // FIND COMPLAINT
        // =====================================================

        IncidentComplaint complaint =
                complaintRepository
                        .findById(
                                complaintId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Complaint not found"
                                )
                        );


        // =====================================================
        // GET INCIDENT
        // =====================================================

        Incident incident =
                complaint.getIncident();


        // =====================================================
        // DETERMINE INCIDENT ZONE
        // =====================================================

        Zone incidentZone =
                getIncidentZone(
                        incident
                );


        // =====================================================
        // ZONE SECURITY
        // =====================================================

        if (
                !incidentZone.name()
                        .equalsIgnoreCase(
                                supervisor.getZone()
                        )
        ) {

            throw new RuntimeException(
                    "You are not allowed to reply to complaints outside your zone"
            );
        }


        // =====================================================
        // SAVE REPLY
        // =====================================================

        complaint.setReply(
                request
                        .getReply()
                        .trim()
        );

        complaint.setRepliedAt(
                LocalDateTime.now()
        );

        complaint.setRepliedBy(
                supervisor.getFullName()
        );

        complaint.setStatus(
                ComplaintStatus.REPLIED
        );


        return complaintRepository.save(
                complaint
        );
    }
}