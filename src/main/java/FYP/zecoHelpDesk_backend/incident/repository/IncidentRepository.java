package FYP.zecoHelpDesk_backend.incident.repository;

import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository
        extends JpaRepository<Incident, Long> {

    Optional<Incident> findByTicketId(String ticketId);

    List<Incident> findBySlaDeadlineBeforeAndSlaAlertSentFalse(
            LocalDateTime time
    );

}