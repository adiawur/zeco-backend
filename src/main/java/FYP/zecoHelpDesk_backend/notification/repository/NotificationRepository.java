package FYP.zecoHelpDesk_backend.notification.repository;

import FYP.zecoHelpDesk_backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {
}