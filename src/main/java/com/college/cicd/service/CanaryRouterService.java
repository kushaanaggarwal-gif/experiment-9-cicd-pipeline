package com.college.cicd.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CanaryRouterService {

    private int rolloutPercentage = 10; // Default 10% Canary traffic
    private final String stableVersion = "v1.0.0";
    private final String canaryVersion = "v2.0.0";

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong stableRequests = new AtomicLong(0);
    private final AtomicLong canaryRequests = new AtomicLong(0);
    private final Random random = new Random();

    public synchronized void setRolloutPercentage(int percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;
        this.rolloutPercentage = percentage;
    }

    public synchronized void rollback() {
        this.rolloutPercentage = 0;
    }

    public synchronized void resetCounters() {
        totalRequests.set(0);
        stableRequests.set(0);
        canaryRequests.set(0);
    }

    public Map<String, Object> routeSingleRequest() {
        long reqId = totalRequests.incrementAndGet();
        int roll = random.nextInt(100); // 0 to 99
        boolean isCanary = roll < rolloutPercentage;

        String routedVersion;
        String releaseType;
        int latencyMs;
        String featureDescription;

        if (isCanary) {
            canaryRequests.incrementAndGet();
            routedVersion = canaryVersion;
            releaseType = "CANARY";
            latencyMs = 18 + random.nextInt(12); // ~18-30ms (optimized new version)
            featureDescription = "High-speed JSON compression + Async query execution (New Canary v2)";
        } else {
            stableRequests.incrementAndGet();
            routedVersion = stableVersion;
            releaseType = "STABLE";
            latencyMs = 38 + random.nextInt(20); // ~38-58ms
            featureDescription = "Standard synchronous database query pipeline (Stable v1)";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requestId", "REQ-" + reqId);
        result.put("rollValue", roll);
        result.put("rolloutThreshold", rolloutPercentage);
        result.put("routedVersion", routedVersion);
        result.put("releaseType", releaseType);
        result.put("latencyMs", latencyMs);
        result.put("httpStatus", 200);
        result.put("feature", featureDescription);
        result.put("timestamp", new Date().toString());
        return result;
    }

    public Map<String, Object> routeBatch(int batchSize) {
        if (batchSize < 1) batchSize = 10;
        if (batchSize > 100) batchSize = 100;

        List<Map<String, Object>> traces = new ArrayList<>();
        int batchStable = 0;
        int batchCanary = 0;

        for (int i = 0; i < batchSize; i++) {
            Map<String, Object> single = routeSingleRequest();
            traces.add(single);
            if ("CANARY".equals(single.get("releaseType"))) {
                batchCanary++;
            } else {
                batchStable++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("batchSize", batchSize);
        summary.put("batchStableCount", batchStable);
        summary.put("batchCanaryCount", batchCanary);
        summary.put("actualCanaryPercentage", (batchCanary * 100.0) / batchSize);
        summary.put("configuredPercentage", rolloutPercentage);
        summary.put("traces", traces);
        return summary;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("rolloutPercentage", rolloutPercentage);
        status.put("stableVersion", stableVersion);
        status.put("canaryVersion", canaryVersion);
        status.put("totalRequests", totalRequests.get());
        status.put("stableRequests", stableRequests.get());
        status.put("canaryRequests", canaryRequests.get());

        // Simulated health check metrics
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", rolloutPercentage > 0 ? "HEALTHY" : "INACTIVE (0% Traffic)");
        health.put("errorRate", "0.02% (Threshold: < 1.0%)");
        health.put("p95Latency", "26 ms");
        health.put("cpuUtilization", "14%");
        health.put("rollbackReady", true);
        status.put("simulatedHealthMetrics", health);

        return status;
    }
}
