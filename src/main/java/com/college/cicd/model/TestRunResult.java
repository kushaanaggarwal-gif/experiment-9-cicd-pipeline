package com.college.cicd.model;

public class TestRunResult {
    private String testName;
    private String category; // "Unit Testing (JUnit 5)", "API Integration (MockMvc)", "Contract Testing (Pact)", "Testcontainers (PostgreSQL)"
    private String status;   // "PASS", "FAIL", "SKIPPED"
    private long durationMs;
    private String description;
    private String expected;
    private String actual;
    private String errorDetails;
    private boolean isRealExecution;

    public TestRunResult() {}

    public TestRunResult(String testName, String category, String status, long durationMs, String description, String expected, String actual, String errorDetails, boolean isRealExecution) {
        this.testName = testName;
        this.category = category;
        this.status = status;
        this.durationMs = durationMs;
        this.description = description;
        this.expected = expected;
        this.actual = actual;
        this.errorDetails = errorDetails;
        this.isRealExecution = isRealExecution;
    }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExpected() { return expected; }
    public void setExpected(String expected) { this.expected = expected; }

    public String getActual() { return actual; }
    public void setActual(String actual) { this.actual = actual; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }

    public boolean isRealExecution() { return isRealExecution; }
    public void setRealExecution(boolean realExecution) { isRealExecution = realExecution; }
}
