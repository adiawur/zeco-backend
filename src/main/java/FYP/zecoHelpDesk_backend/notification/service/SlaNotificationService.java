package FYP.zecoHelpDesk_backend.notification.service;

import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentRepository;
import FYP.zecoHelpDesk_backend.user.entity.User;
import FYP.zecoHelpDesk_backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlaNotificationService {

    private final IncidentRepository incidentRepository;

    private final UserRepository userRepository;

    private final EmailNotificationService emailNotificationService;


    // =========================================================
    // CHECK SLA EVERY MINUTE
    // =========================================================

    @Scheduled(fixedRate = 60000)
    public void checkSlaBreaches() {

        LocalDateTime now =
                LocalDateTime.now();


        List<Incident> incidents =
                incidentRepository
                        .findBySlaDeadlineBeforeAndSlaAlertSentFalse(
                                now
                        );


        for (Incident incident : incidents) {

            // -------------------------------------------------
            // Only HIGH priority incidents have SLA configured
            // -------------------------------------------------

            if (
                    incident.getSlaDeadline() == null
            ) {

                continue;
            }


            // -------------------------------------------------
            // Only incidents with NO ACTION
            // -------------------------------------------------

            if (
                    incident.getStatus()
                            != FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus.REPORTED
            ) {

                continue;
            }


            // -------------------------------------------------
            // SEND ALERT
            // -------------------------------------------------

            sendSlaAlert(
                    incident
            );


            // -------------------------------------------------
            // PREVENT DUPLICATE EMAIL
            // -------------------------------------------------

            incident.setSlaAlertSent(
                    true
            );

            incidentRepository.save(
                    incident
            );
        }
    }


    // =========================================================
    // SEND SLA ALERT TO SUPERVISORS / ADMINS
    // =========================================================

    private void sendSlaAlert(
            Incident incident
    ) {

        List<User> users =
                userRepository.findAll();


        for (User user : users) {

            if (
                    user.getRole() == null
            ) {

                continue;
            }


            String role =
                    user.getRole()
                            .name();


            if (
                    !role.equals("ADMIN")
                            &&
                            !role.equals("SUPERVISOR")
            ) {

                continue;
            }


            if (
                    user.getEmail() == null
                            ||
                            user.getEmail().isBlank()
            ) {

                continue;
            }


            emailNotificationService
                    .sendSlaAlert(

                            user.getEmail(),

                            user.getFullName(),

                            incident.getTicketId(),

                            incident.getIncidentType().name(),

                            incident.getPriority().name(),

                            incident.getLocation(),

                            incident.getSlaDeadline()
                    );
        }
    }
}