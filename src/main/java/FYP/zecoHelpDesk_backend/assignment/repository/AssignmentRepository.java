package FYP.zecoHelpDesk_backend.assignment.repository;

import FYP.zecoHelpDesk_backend.assignment.entity.Assignment;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository
        extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findByIncident(Incident incident);

    List<Assignment> findByTechnician(User technician);
}