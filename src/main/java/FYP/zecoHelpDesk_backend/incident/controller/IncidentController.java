package FYP.zecoHelpDesk_backend.incident.controller;

import FYP.zecoHelpDesk_backend.incident.dto.IncidentComplaintRequest;
import FYP.zecoHelpDesk_backend.incident.dto.IncidentRequest;
import FYP.zecoHelpDesk_backend.incident.dto.IncidentResponse;
import FYP.zecoHelpDesk_backend.incident.entity.Incident;
import FYP.zecoHelpDesk_backend.incident.entity.IncidentComplaint;
import FYP.zecoHelpDesk_backend.incident.repository.IncidentRepository;
import FYP.zecoHelpDesk_backend.incident.service.IncidentComplaintService;
import FYP.zecoHelpDesk_backend.incident.service.IncidentService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@CrossOrigin("*")
public class IncidentController {

    private final IncidentService service;

    private final IncidentRepository repository;

    private final ObjectMapper objectMapper;

    private final IncidentComplaintService complaintService;


    // =========================================================
    // REPORT INCIDENT
    // =========================================================

    @PostMapping(
            value = "/report",
            consumes = "multipart/form-data"
    )
    public IncidentResponse report(

            @RequestPart("data")
            String data,

            @RequestPart(
                    value = "photo",
                    required = false
            )
            MultipartFile photo

    ) throws Exception {

        IncidentRequest request =
                objectMapper.readValue(
                        data,
                        IncidentRequest.class
                );


        return service.report(
                request,
                photo
        );
    }


    // =========================================================
    // PUBLIC TRACK INCIDENTS
    //
    // NO LOGIN REQUIRED
    //
    // fullName + phone required
    // email optional
    // =========================================================

    @PostMapping("/track")
    public List<IncidentResponse> trackIncidents(

            @Valid
            @RequestBody
            FYP.zecoHelpDesk_backend.incident.dto.TrackIncidentRequest request

    ) {

        return service.trackIncidents(
                request
        );
    }


    // =========================================================
    // GET ALL INCIDENTS
    // =========================================================

    @GetMapping
    public List<IncidentResponse> getAll() {

        return repository.findAll()
                .stream()
                .map(service::toResponse)
                .toList();
    }


    // =========================================================
    // GET INCIDENT BY ID
    // =========================================================

    @GetMapping("/{id}")
    public IncidentResponse getById(

            @PathVariable Long id

    ) {

        Incident incident =
                repository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Incident not found"
                                )
                        );


        return service.toResponse(
                incident
        );
    }


    // =========================================================
    // GET INCIDENT BY TICKET ID
    // =========================================================

    @GetMapping("/ticket/{ticketId}")
    public IncidentResponse getByTicket(

            @PathVariable String ticketId

    ) {

        Incident incident =
                repository.findByTicketId(
                                ticketId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Incident not found"
                                )
                        );


        return service.toResponse(
                incident
        );
    }

    // =========================================================
// PUBLIC CUSTOMER COMPLAINT
// =========================================================

    @PostMapping("/complaint")
    public IncidentComplaint submitComplaint(

            @Valid
            @RequestBody
            IncidentComplaintRequest request

    ) {

        return complaintService.submit(
                request
        );
    }
}