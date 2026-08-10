package FYP.zecoHelpDesk_backend.report.repository;

import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentStatus;
import FYP.zecoHelpDesk_backend.incident.entity.Priority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository
        extends JpaRepository<Incident, Long> {


    // =========================================================
    // STATUS COUNTS
    // =========================================================

    @Query("""
            SELECT i.status, COUNT(i)
            FROM Incident i
            GROUP BY i.status
            """)
    List<Object[]> countByStatus();


    // =========================================================
    // PRIORITY COUNTS
    // =========================================================

    @Query("""
            SELECT i.priority, COUNT(i)
            FROM Incident i
            GROUP BY i.priority
            """)
    List<Object[]> countByPriority();


    // =========================================================
    // INCIDENT TYPE COUNTS
    // =========================================================

    @Query("""
            SELECT i.incidentType, COUNT(i)
            FROM Incident i
            GROUP BY i.incidentType
            """)
    List<Object[]> countByIncidentType();


    // =========================================================
    // TOTAL
    // =========================================================

    long count();


    // =========================================================
    // STATUS
    // =========================================================

    long countByStatus(
            IncidentStatus status
    );


    // =========================================================
    // PRIORITY
    // =========================================================

    long countByPriority(
            Priority priority
    );
}