package com.college.cicd.model;

import java.util.List;

public class TestSuiteSummary {
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;
    private long totalDurationMs;
    private String overallStatus; // "PASSED", "FAILED"
    private String executionMode; // "Real In-JVM & Spring Test Execution"
    private List<TestRunResult> results;
    private String timestamp;

    public TestSuiteSummary() {}

    public TestSuiteSummary(int totalTests, int passedTests, int failedTests, int skippedTests, long totalDurationMs, String overallStatus, String executionMode, List<TestRunResult> results, String timestamp) {
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.skippedTests = skippedTests;
        this.totalDurationMs = totalDurationMs;
        this.overallStatus = overallStatus;
        this.executionMode = executionMode;
        this.results = results;
        this.timestamp = timestamp;
    }

    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }

    public int getPassedTests() { return passedTests; }
    public void setPassedTests(int passedTests) { this.passedTests = passedTests; }

    public int getFailedTests() { return failedTests; }
    public void setFailedTests(int failedTests) { this.failedTests = failedTests; }

    public int getSkippedTests() { return skippedTests; }
    public void setSkippedTests(int skippedTests) { this.skippedTests = skippedTests; }

    public long getTotalDurationMs() { return totalDurationMs; }
    public void setTotalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; }

    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }

    public String getExecutionMode() { return executionMode; }
    public void setExecutionMode(String executionMode) { this.executionMode = executionMode; }

    public List<TestRunResult> getResults() { return results; }
    public void setResults(List<TestRunResult> results) { this.results = results; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
