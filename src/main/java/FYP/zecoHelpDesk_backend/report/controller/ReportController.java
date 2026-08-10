package FYP.zecoHelpDesk_backend.report.controller;

import FYP.zecoHelpDesk_backend.report.dto.DashboardResponse;
import FYP.zecoHelpDesk_backend.report.dto.PriorityReportResponse;
import FYP.zecoHelpDesk_backend.report.dto.SlaReportResponse;
import FYP.zecoHelpDesk_backend.report.dto.StatusReportResponse;
import FYP.zecoHelpDesk_backend.report.dto.TypeReportResponse;
import FYP.zecoHelpDesk_backend.report.service.ReportService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReportController {

    private final ReportService service;


    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {

        return service.getDashboard();
    }


    // =========================================================
    // STATUS REPORT
    // =========================================================

    @GetMapping("/status")
    public List<StatusReportResponse> status() {

        return service.getStatusReport();
    }


    // =========================================================
    // PRIORITY REPORT
    // =========================================================

    @GetMapping("/priority")
    public List<PriorityReportResponse> priority() {

        return service.getPriorityReport();
    }


    // =========================================================
    // INCIDENT TYPE REPORT
    // =========================================================

    @GetMapping("/types")
    public List<TypeReportResponse> types() {

        return service.getTypeReport();
    }


    // =========================================================
    // SLA REPORT
    // =========================================================

    @GetMapping("/sla")
    public List<SlaReportResponse> sla() {

        return service.getSlaReport();
    }
}