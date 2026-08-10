package FYP.zecoHelpDesk_backend.notification.controller;

import FYP.zecoHelpDesk_backend.notification.dto.NotificationRequest;
import FYP.zecoHelpDesk_backend.notification.entity.Notification;
import FYP.zecoHelpDesk_backend.notification.service.EmailNotificationService;
import FYP.zecoHelpDesk_backend.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

    private final EmailNotificationService emailService;

    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(
            @Valid @RequestBody NotificationRequest request
    ) {

        emailService.sendEmail(request);

        return ResponseEntity.ok(
                "Email sent successfully"
        );
    }

    @GetMapping
    public List<Notification> getAll() {

        return notificationService.getAll();

    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable Long id
    ) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok(
                "Notification marked as read"
        );
    }

    @DeleteMapping
    public ResponseEntity<String> clearAll() {

        notificationService.clearAll();

        return ResponseEntity.ok(
                "Notifications cleared successfully"
        );
    }
}