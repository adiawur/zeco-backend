package FYP.zecoHelpDesk_backend.notification.service;

import FYP.zecoHelpDesk_backend.notification.entity.Notification;
import FYP.zecoHelpDesk_backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public Notification create(
            String type,
            String title,
            String message
    ) {

        Notification notification =
                Notification.builder()
                        .type(type)
                        .title(title)
                        .message(message)
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        return repository.save(notification);
    }

    public List<Notification> getAll() {

        return repository.findAll(
                org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        );
    }

    public void markAsRead(Long id) {

        Notification notification =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification not found"
                                )
                        );

        notification.setRead(true);

        repository.save(notification);
    }

    public void clearAll() {

        repository.deleteAll();

    }
}