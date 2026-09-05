package FYP.zecoHelpDesk_backend.incident.repository;

import FYP.zecoHelpDesk_backend.incident.entity.IncidentComplaint;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentComplaintRepository
        extends JpaRepository<IncidentComplaint, Long> {

    List<IncidentComplaint> findByIncident(Incident incident);

}