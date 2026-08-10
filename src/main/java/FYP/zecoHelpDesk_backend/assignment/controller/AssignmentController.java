package FYP.zecoHelpDesk_backend.assignment.controller;

import FYP.zecoHelpDesk_backend.assignment.dto.AssignmentRequest;
import FYP.zecoHelpDesk_backend.assignment.dto.AssignmentResponse;
import FYP.zecoHelpDesk_backend.assignment.dto.CompleteAssignmentRequest;
import FYP.zecoHelpDesk_backend.assignment.service.AssignmentService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AssignmentController {

    private final AssignmentService service;

    private final ObjectMapper objectMapper;


    // =========================================================
    // SUPERVISOR
    // ASSIGN INCIDENT TO TECHNICIAN
    // =========================================================

    @PostMapping("/supervisor/assignments")
    public AssignmentResponse assign(

            @Valid
            @RequestBody
            AssignmentRequest request

    ) {

        return service.assign(
                request
        );
    }


    // =========================================================
    // SUPERVISOR
    // GET ALL ASSIGNMENTS
    // =========================================================

    @GetMapping("/supervisor/assignments")
    public List<AssignmentResponse> getAll() {

        return service.getAll();
    }


    // =========================================================
    // SUPERVISOR
    // GET ASSIGNMENT BY ID
    // =========================================================

    @GetMapping("/supervisor/assignments/{id}")
    public AssignmentResponse getById(

            @PathVariable Long id

    ) {

        return service.getById(
                id
        );
    }


    // =========================================================
    // SUPERVISOR
    // GET ASSIGNMENTS BY TECHNICIAN
    // =========================================================

    @GetMapping(
            "/supervisor/assignments/technician/{technicianId}"
    )
    public List<AssignmentResponse> getByTechnician(

            @PathVariable Long technicianId

    ) {

        return service.getByTechnician(
                technicianId
        );
    }


    // =========================================================
    // SUPERVISOR
    // RESOLVE INCIDENT
    // =========================================================

    @PatchMapping(
            "/supervisor/assignments/{id}/resolve"
    )
    public AssignmentResponse resolve(

            @PathVariable Long id

    ) {

        return service.resolve(
                id
        );
    }


    // =========================================================
    // TECHNICIAN
    // GET MY ASSIGNMENTS
    // =========================================================

    @GetMapping(
            "/technician/assignments"
    )
    public List<AssignmentResponse> getMyAssignments(

            Authentication authentication

    ) {

        return service.getByTechnicianUsername(
                authentication.getName()
        );
    }


    // =========================================================
    // TECHNICIAN
    // GET MY ASSIGNMENT BY ID
    // =========================================================

    @GetMapping(
            "/technician/assignments/{id}"
    )
    public AssignmentResponse getMyAssignmentById(

            @PathVariable Long id,

            Authentication authentication

    ) {

        return service.getMyAssignmentById(
                id,
                authentication
        );
    }


    // =========================================================
    // TECHNICIAN
    // START WORK
    // =========================================================

    @PatchMapping(
            "/technician/assignments/{id}/start"
    )
    public AssignmentResponse startWork(

            @PathVariable Long id,

            Authentication authentication

    ) {

        return service.startWork(
                id,
                authentication
        );
    }

    // =========================================================
// TECHNICIAN
// COMPLETE WORK
// =========================================================

    @PostMapping(
            value = "/technician/assignments/{id}/complete",
            consumes = "multipart/form-data"
    )
    public AssignmentResponse completeWork(

            @PathVariable Long id,

            @RequestPart("data")
            String data,

            @RequestPart(
                    value = "photo",
                    required = false
            )
            MultipartFile photo,

            Authentication authentication

    ) throws Exception {

        CompleteAssignmentRequest request =
                objectMapper.readValue(
                        data,
                        CompleteAssignmentRequest.class
                );


        return service.completeWork(

                id,

                request,

                photo,

                authentication
        );
    }
}