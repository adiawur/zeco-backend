package FYP.zecoHelpDesk_backend.incident.repository;

import FYP.zecoHelpDesk_backend.incident.entity.IncidentComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentComplaintRepository
        extends JpaRepository<IncidentComplaint, Long> {
}