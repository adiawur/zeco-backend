package FYP.zecoHelpDesk_backend.notification.service;

import FYP.zecoHelpDesk_backend.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;


    // =========================================================
    // GENERAL EMAIL
    // =========================================================

    public void sendEmail(
            NotificationRequest request
    ) {

        SimpleMailMessage mail =
                new SimpleMailMessage();


        mail.setTo(
                request.getTo()
        );


        mail.setSubject(
                request.getSubject()
        );


        String body =

                "ZECO HELP DESK\n"
                        + "==============================\n\n"

                        + request.getTitle()
                        + "\n\n"

                        + request.getMessage()
                        + "\n\n"

                        + "Please log in to ZECO Help Desk "
                        + "for more details.\n\n"

                        + "Regards,\n"
                        + "ZECO Help Desk";


        mail.setText(body);


        mailSender.send(mail);
    }


    // =========================================================
    // TECHNICIAN ASSIGNMENT EMAIL
    // =========================================================

    public void sendAssignmentNotification(

            String technicianEmail,

            String technicianName,

            String ticketId,

            String incidentType,

            String priority,

            String location

    ) {

        NotificationRequest request =
                NotificationRequest.builder()

                        .to(
                                technicianEmail
                        )

                        .subject(
                                "New Incident Assignment - "
                                        + ticketId
                        )

                        .title(
                                "New Incident Assigned"
                        )

                        .message(

                                "Hello "
                                        + technicianName
                                        + ",\n\n"

                                        + "You have been assigned "
                                        + "a new incident.\n\n"

                                        + "Ticket ID: "
                                        + ticketId
                                        + "\n"

                                        + "Incident Type: "
                                        + incidentType
                                        + "\n"

                                        + "Priority: "
                                        + priority
                                        + "\n"

                                        + "Location: "
                                        + location
                                        + "\n\n"

                                        + "Please log in to "
                                        + "ZECO Help Desk to "
                                        + "view and process "
                                        + "the assignment."
                        )

                        .build();


        sendEmail(request);
    }


    // =========================================================
    // CUSTOMER STATUS EMAIL
    // =========================================================

    public void sendStatusNotification(

            String customerEmail,

            String ticketId,

            String status

    ) {

        NotificationRequest request =
                NotificationRequest.builder()

                        .to(
                                customerEmail
                        )

                        .subject(
                                "Incident Update - "
                                        + ticketId
                        )

                        .title(
                                "Incident Status Updated"
                        )

                        .message(

                                "Your incident with "
                                        + "ticket ID "
                                        + ticketId
                                        + " has been updated.\n\n"

                                        + "Current Status: "
                                        + status
                                        + "\n\n"

                                        + "Please log in to "
                                        + "ZECO Help Desk "
                                        + "to view more details."
                        )

                        .build();


        sendEmail(request);
    }


    // =========================================================
    // SLA ALERT EMAIL
    // =========================================================

    public void sendSlaAlert(

            String email,

            String name,

            String ticketId,

            String incidentType,

            String priority,

            String location,

            LocalDateTime slaDeadline

    ) {

        NotificationRequest request =
                NotificationRequest.builder()

                        .to(
                                email
                        )

                        .subject(
                                "SLA Alert - Incident "
                                        + ticketId
                        )

                        .title(
                                "SLA Deadline Alert"
                        )

                        .message(

                                "Dear "
                                        + name
                                        + ",\n\n"

                                        + "The following incident "
                                        + "has exceeded the configured "
                                        + "SLA timeframe without action.\n\n"

                                        + "Ticket ID: "
                                        + ticketId
                                        + "\n"

                                        + "Incident Type: "
                                        + incidentType
                                        + "\n"

                                        + "Priority: "
                                        + priority
                                        + "\n"

                                        + "Location: "
                                        + location
                                        + "\n"

                                        + "SLA Deadline: "
                                        + slaDeadline
                                        + "\n\n"

                                        + "Please review the incident "
                                        + "and take the necessary action.\n\n"

                                        + "ZECO Help Desk Management System"
                        )

                        .build();


        sendEmail(request);
    }
}