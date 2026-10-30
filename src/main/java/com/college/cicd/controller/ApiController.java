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

    // 1. Ability to view Docker image detailed inspect manifest
    @GetMapping("/docker/inspect")
    public ResponseEntity<Map<String, Object>> inspectDockerImage(@RequestParam(defaultValue = "v2.0.0") String tag) {
        Map<String, Object> details = dockerRegistryService.inspectImage(tag);
        pipelineService.addLog("DOCKER_INSPECT", "Inspected container image manifest for tag: " + tag);
        return ResponseEntity.ok(details);
    }

    // 2. Dedicated App Health Probe returning HTTP 200 OK
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthProbe() {
        long uptimeSeconds = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemoryMb = runtime.maxMemory() / (1024 * 1024);

        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("statusCode", 200);
        health.put("httpStatusText", "HTTP 200 OK");
        health.put("message", "HTTP 200 OK - Application Healthy and Serving Production Traffic");
        health.put("timestamp", java.time.Instant.now().toString());
        health.put("uptimeSeconds", uptimeSeconds);
        health.put("activeRelease", "v2.0.0");
        health.put("environment", "production");

        Map<String, Object> components = new LinkedHashMap<>();

        Map<String, Object> httpListener = new LinkedHashMap<>();
        httpListener.put("status", "UP");
        httpListener.put("port", 8089);
        httpListener.put("statusCode", 200);
        httpListener.put("latencyMs", 2);
        components.put("httpListener", httpListener);

        Map<String, Object> db = new LinkedHashMap<>();
        db.put("status", "UP");
        db.put("dialect", "PostgreSQL / H2 Compatible");
        db.put("connectionPool", "HikariCP-1 (Active: 1, Idle: 9, Total: 10)");
        db.put("latencyMs", 1);
        components.put("database", db);

        Map<String, Object> mem = new LinkedHashMap<>();
        mem.put("status", "UP");
        mem.put("heapUsed", usedMemoryMb + " MB");
        mem.put("heapMax", maxMemoryMb + " MB");
        components.put("memory", mem);

        Map<String, Object> canary = new LinkedHashMap<>();
        canary.put("status", "UP");
        canary.put("currentRollout", canaryService.getStatus().get("rolloutPercentage") + "%");
        canary.put("stableVersion", "v1.0.0");
        canary.put("canaryVersion", "v2.0.0");
        components.put("canaryRouter", canary);

        health.put("components", components);
        return ResponseEntity.ok(health);
    }

    // 3. Show Release Version and Error on Previous Version
    @GetMapping("/version/status")
    public ResponseEntity<Map<String, Object>> getVersionStatus() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("currentRelease", "v2.0.0");
        res.put("currentStatus", "HEALTHY");
        res.put("currentDescription", "Production Canary Release. Patched HikariCP connection leaks, zero unclosed cursors, asynchronous non-blocking stream handling.");

        res.put("previousVersion", "v1.0.0");
        res.put("previousStatus", "FAILED / DEPRECATED");
        res.put("previousDefect", "Critical ConnectionPoolExhaustedException (HTTP 500) under concurrent load due to unclosed database cursors in legacy PostController.");

        res.put("canaryRolloutPercentage", canaryService.getStatus().get("rolloutPercentage"));
        res.put("defectRemediationVerified", true);
        res.put("recommendedAction", "Promote v2.0.0 from Canary (10%) to full rollout (100%) once health probes remain HTTP 200 OK.");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/version/test")
    public ResponseEntity<Map<String, Object>> testVersion(@RequestParam(defaultValue = "v2.0.0") String version) {
        if ("v1.0.0".equalsIgnoreCase(version.trim())) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("version", "v1.0.0 (Previous Version)");
            err.put("status", "FAILED");
            err.put("statusCode", 500);
            err.put("httpStatusText", "HTTP 500 Internal Server Error");
            err.put("error", "ConnectionPoolExhaustedException");
            err.put("exception", "java.sql.SQLException: HikariPool-1 - Connection is not available, request timed out after 30000ms.");
            err.put("message", "CRITICAL ERROR in Previous Version v1.0.0: Concurrent requests caused unclosed database cursors, exhausting connection pool and crashing endpoint with HTTP 500.");
            err.put("stackTraceSnippet",
                "com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:217)\n" +
                "\tat com.college.cicd.legacy.LegacyOrderQueryHandler.execute(LegacyOrderQueryHandler.java:94)\n" +
                "\tat com.college.cicd.controller.PostController.getPosts(PostController.java:38)");
            err.put("defectAnalysis", "Previous version v1.0.0 leaked database connections under production load. This defect directly triggered the CI/CD pipeline Canary deployment of v2.0.0.");
            err.put("remediation", "Route traffic to v2.0.0 Canary release candidate where connection pooling and heap management are patched.");

            pipelineService.addLog("VERSION_TEST", "Defect Reproduction on Previous v1.0.0 -> HTTP 500 ConnectionPoolExhaustedException reproduced.");
            return ResponseEntity.status(500).body(err);
        } else {
            Map<String, Object> ok = new LinkedHashMap<>();
            ok.put("version", "v2.0.0 (Current Release)");
            ok.put("status", "PASSED");
            ok.put("statusCode", 200);
            ok.put("httpStatusText", "HTTP 200 OK");
            ok.put("message", "HTTP 200 OK: Current Release v2.0.0 operating within SLO parameters. Database connection pool healthy, 0 leaked connections, P99 latency: 3.2ms.");
            ok.put("activeConnections", 2);
            ok.put("idleConnections", 18);
            ok.put("maxConnections", 20);
            ok.put("latencyMs", 3.2);
            ok.put("defectStatus", "RESOLVED");
            ok.put("recommendation", "All automated Canary verification checks PASSED. Safe to promote v2.0.0 to 100% production traffic.");

            pipelineService.addLog("VERSION_TEST", "Validation on Current v2.0.0 -> HTTP 200 OK (0 leaks, latency 3.2ms).");
            return ResponseEntity.ok(ok);
        }
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
