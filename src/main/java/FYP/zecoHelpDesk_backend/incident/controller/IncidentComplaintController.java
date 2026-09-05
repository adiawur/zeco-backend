package FYP.zecoHelpDesk_backend.incident.controller;

import FYP.zecoHelpDesk_backend.incident.dto.ComplaintReplyRequest;
import FYP.zecoHelpDesk_backend.incident.dto.IncidentComplaintRequest;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentComplaint;
import FYP.zecoHelpDesk_backend.incident.service.IncidentComplaintService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class IncidentComplaintController {

    private final IncidentComplaintService service;


    // =====================================================
    // CUSTOMER
    // SUBMIT COMPLAINT / FEEDBACK
    // =====================================================

    @PostMapping("/complaints")
    public IncidentComplaint submit(

            @Valid
            @RequestBody
            IncidentComplaintRequest request

    ) {

        return service.submit(
                request
        );
    }


    // =====================================================
    // SUPERVISOR
    // GET MY ZONE COMPLAINTS / FEEDBACK
    // =====================================================

    @GetMapping("/supervisor/complaints")
    public List<IncidentComplaint> getSupervisorComplaints(

            Authentication authentication

    ) {

        return service.getSupervisorComplaints(
                authentication
        );
    }


    // =====================================================
    // SUPERVISOR
    // REPLY
    // =====================================================

    @PatchMapping(
            "/supervisor/complaints/{id}/reply"
    )
    public IncidentComplaint reply(

            @PathVariable Long id,

            @Valid
            @RequestBody
            ComplaintReplyRequest request,

            Authentication authentication

    ) {

        return service.replyToComplaint(
                id,
                request,
                authentication
        );
    }
}