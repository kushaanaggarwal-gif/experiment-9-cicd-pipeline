package com.college.cicd.controller;

import com.college.cicd.model.CalculatorResult;
import com.college.cicd.model.TestSuiteSummary;
import com.college.cicd.service.CalculatorService;
import com.college.cicd.service.CanaryRouterService;
import com.college.cicd.service.ContractVerificationService;
import com.college.cicd.service.PipelineExecutionService;
import com.college.cicd.service.GitRepositoryService;
import com.college.cicd.service.DockerRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private CalculatorService calculatorService;

    @Autowired
    private ContractVerificationService contractService;

    @Autowired
    private CanaryRouterService canaryService;

    @Autowired
    private PipelineExecutionService pipelineService;

    @Autowired
    private GitRepositoryService gitService;

    @Autowired
    private DockerRegistryService dockerRegistryService;

    // Overview & Status
    @GetMapping("/pipeline/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(pipelineService.getPipelineOverview());
    }

    // Automated Test Suite Execution
    @PostMapping("/pipeline/run-tests")
    public ResponseEntity<TestSuiteSummary> runAutomatedTests() {
        TestSuiteSummary summary = pipelineService.executeAutomatedTestSuite();
        return ResponseEntity.ok(summary);
    }

    // Local Maven Build Execution
    @PostMapping("/pipeline/run-build")
    public ResponseEntity<Map<String, Object>> runBuild() {
        Map<String, Object> result = pipelineService.runLocalMavenBuild();
        return ResponseEntity.ok(result);
    }

    // Unit Testing Calculator Interactive Endpoint
    @GetMapping("/testing/unit/calculator")
    public ResponseEntity<CalculatorResult> runCalculatorTest(
            @RequestParam(defaultValue = "15.0") double a,
            @RequestParam(defaultValue = "25.0") double b,
            @RequestParam(defaultValue = "40.0") double expected) {
        CalculatorResult result = calculatorService.testAddition(a, b, expected);
        pipelineService.addLog("UNIT_TEST", String.format("JUnit 5 Calculator test: add(%.1f, %.1f) == %.1f -> %s", a, b, expected, result.getStatus()));
        return ResponseEntity.ok(result);
    }

    // Contract Testing Pact Verification Endpoint
    @GetMapping("/testing/contract")
    public ResponseEntity<Map<String, Object>> verifyContract(
            @RequestParam(defaultValue = "false") boolean simulateDrift) {
        Map<String, Object> result = contractService.verifyContract(simulateDrift);
        pipelineService.addLog("PACT_CONTRACT", String.format("Pact contract check (simulateDrift=%b) -> %s", simulateDrift, result.get("contractStatus")));
        return ResponseEntity.ok(result);
    }

    // Testcontainers status & info
    @GetMapping("/testing/testcontainers/info")
    public ResponseEntity<Map<String, Object>> getTestcontainersInfo() {
        Map<String, Object> dockerStatus = pipelineService.checkDockerAvailability();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("serviceTested", "PostgreSQL 15 (Docker Image: postgres:15-alpine)");
        info.put("dockerAvailable", dockerStatus.get("available"));
        info.put("dockerMessage", dockerStatus.get("message"));
        info.put("configurationExample", 
            "@Container\n" +
            "static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(\"postgres:15-alpine\")\n" +
            "    .withDatabaseName(\"testdb\")\n" +
            "    .withUsername(\"postgres\")\n" +
            "    .withPassword(\"secret\");");
        return ResponseEntity.ok(info);
    }

    // Docker Status and Build Attempt
    // Git Repository & CI Pipeline Details
    @GetMapping("/git/status")
    public ResponseEntity<Map<String, Object>> getGitStatus() {
        return ResponseEntity.ok(gitService.getRepositoryStatus());
    }

    // Docker Registry Images Catalog
    @GetMapping("/docker/images")
    public ResponseEntity<List<Map<String, Object>>> getDockerImages() {
        return ResponseEntity.ok(dockerRegistryService.getRegisteredImages());
    }

    // Docker Status and Build
    @GetMapping("/pipeline/docker-status")
    public ResponseEntity<Map<String, Object>> getDockerStatus() {
        return ResponseEntity.ok(pipelineService.checkDockerAvailability());
    }

    @PostMapping("/pipeline/docker-build")
    public ResponseEntity<Map<String, Object>> attemptDockerBuild(@RequestParam(defaultValue = "v2.0.0") String tag) {
        Map<String, Object> result = dockerRegistryService.buildDockerImage(tag);
        return ResponseEntity.ok(result);
    }

    // Canary Deployment Endpoints
    @GetMapping("/canary/status")
    public ResponseEntity<Map<String, Object>> getCanaryStatus() {
        return ResponseEntity.ok(canaryService.getStatus());
    }

    @PostMapping("/canary/set-rollout")
    public ResponseEntity<Map<String, Object>> setCanaryRollout(@RequestParam int percentage) {
        canaryService.setRolloutPercentage(percentage);
        pipelineService.addLog("CANARY_RELEASE", "Traffic rollout percentage updated to: " + percentage + "%");
        return ResponseEntity.ok(canaryService.getStatus());
    }

    @PostMapping("/canary/rollback")
    public ResponseEntity<Map<String, Object>> rollbackCanary() {
        canaryService.rollback();
        pipelineService.addLog("CANARY_ROLLBACK", "ALERT: Canary rolled back to 0%! All traffic directed to Stable v1.0.0.");
        return ResponseEntity.ok(canaryService.getStatus());
    }

    @PostMapping("/canary/reset-counters")
    public ResponseEntity<Map<String, Object>> resetCounters() {
        canaryService.resetCounters();
        return ResponseEntity.ok(canaryService.getStatus());
    }

    @GetMapping("/canary/route")
    public ResponseEntity<Map<String, Object>> routeCanary() {
        return ResponseEntity.ok(canaryService.routeSingleRequest());
    }

    @PostMapping("/canary/batch-route")
    public ResponseEntity<Map<String, Object>> batchRouteCanary(@RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(canaryService.routeBatch(count));
    }

    // Execution Logs
    @GetMapping("/pipeline/logs")
    public ResponseEntity<List<String>> getLogs() {
        return ResponseEntity.ok(pipelineService.getLogs());
    }

    @PostMapping("/pipeline/logs/clear")
    public ResponseEntity<Map<String, String>> clearLogs() {
        pipelineService.clearLogs();
        Map<String, String> res = new HashMap<>();
        res.put("message", "Logs cleared successfully.");
        return ResponseEntity.ok(res);
    }
}
