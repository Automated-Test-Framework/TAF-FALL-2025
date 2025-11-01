package ca.etsmtl.taf.controller;

import ca.etsmtl.taf.dto.*;
import ca.etsmtl.taf.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = { "http://localhost:4200", "http://localhost:5173" })
public class DashboardController {

    private final DashboardService service;
    private final DashboardService dashboardService;

    public DashboardController(DashboardService service, DashboardService dashboardService) {
        this.service = service;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/report")
    public List<RunCardDto> latestRuns(
            @RequestParam String project,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "5") int limit) {
        return service.latestRuns(project, status, limit);
    }

    @GetMapping("/report/run/{runId}")
    public ResponseEntity<RunDetailDto> runById(@PathVariable String runId) {
        RunDetailDto d = service.runById(runId);
        return d == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(d);
    }

    @GetMapping("/cases")
    public Map<String,Object> searchCases(
            @RequestParam String project,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String tool,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.searchCases(project, type, tool, status, from, to, page, size);
    }

    @GetMapping("/summary/passrate")
    public List<PassratePointDto> passrate(
            @RequestParam String project,
            @RequestParam(defaultValue = "14") int days) {
        return service.passrate(project, days);
    }


    @GetMapping("/summary/by-tool")
    public List<ToolStatDto> byTool(
            @RequestParam String project,
            @RequestParam(defaultValue = "30") int days) {
        return dashboardService.statsByTool(project, days);
    }

    @GetMapping("/summary/top-fails")
    public List<NamedCountDto> topFails(
            @RequestParam String project,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "5") int limit) {
        return dashboardService.topFailingTests(project, days, limit);
    }

    @GetMapping("/summary/flaky")
    public List<NamedCountDto> flaky(
            @RequestParam String project,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "5") int limit) {
        return dashboardService.flakyTests(project, days, limit);
    }


    @GetMapping("/dashboard/summary/tool-passrate")
    public List<ToolRateDto> toolPassrate(
            @RequestParam String project,
            @RequestParam(required = false, defaultValue = "30") int days
    ) {
        return dashboardService.passrateByTool(project, days);
    }


    @GetMapping("/summary/by-type")
    public List<Map> byType(
            @RequestParam String project,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tool
    ) {
        return dashboardService.passrateByType(project, days, status, tool);
    }

    @GetMapping("/summary/avg-duration")
    public List<Map> avgDuration(
            @RequestParam String project,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(required = false) String tool
    ) {
        return dashboardService.avgDuration(project, days, tool);
    }


}
