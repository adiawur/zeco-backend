package FYP.zecoHelpDesk_backend.incident.service;

import FYP.zecoHelpDesk_backend.incident.dto.IncidentComplaintRequest;
import FYP.zecoHelpDesk_backend.incident.entity.ComplaintStatus;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentComplaint;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentComplaintRepository;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentComplaintService {

    private final IncidentRepository incidentRepository;

    private final IncidentComplaintRepository complaintRepository;


    // =========================================================
    // SUBMIT CUSTOMER COMPLAINT
    // =========================================================

    public IncidentComplaint submit(
            IncidentComplaintRequest request
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "Complaint information is required"
            );
        }


        // -----------------------------------------------------
        // FIND INCIDENT
        // -----------------------------------------------------

        Incident incident =
                incidentRepository
                        .findByTicketId(
                                request.getTicketId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Incident not found"
                                )
                        );


        // -----------------------------------------------------
        // VERIFY CUSTOMER DETAILS
        // -----------------------------------------------------

        if (
                !incident
                        .getReporterName()
                        .equalsIgnoreCase(
                                request.getFullName().trim()
                        )
        ) {

            throw new RuntimeException(
                    "The provided information does not match this incident"
            );
        }


        if (
                !incident
                        .getPhone()
                        .equals(
                                request.getPhone().trim()
                        )
        ) {

            throw new RuntimeException(
                    "The provided information does not match this incident"
            );
        }


        // -----------------------------------------------------
        // FINAL STATUS CHECK
        // -----------------------------------------------------

        if (
                incident.getStatus()
                        == IncidentStatus.COMPLETED
                        ||
                        incident.getStatus()
                                == IncidentStatus.RESOLVED
                        ||
                        incident.getStatus()
                                == IncidentStatus.CLOSED
        ) {

            throw new RuntimeException(
                    "Complaint cannot be submitted because this incident has already been completed"
            );
        }


        // -----------------------------------------------------
        // SAVE COMPLAINT
        // -----------------------------------------------------

        IncidentComplaint complaint =
                IncidentComplaint.builder()

                        .incident(incident)

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
                                request.getEmail()
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


        return complaintRepository.save(
                complaint
        );
    }
}