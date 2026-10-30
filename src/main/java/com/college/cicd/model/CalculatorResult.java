package com.college.cicd.model;

public class CalculatorResult {
    private double a;
    private double b;
    private String operation;
    private double expected;
    private double actual;
    private boolean passed;
    private String status;
    private String message;

    public CalculatorResult() {}

    public CalculatorResult(double a, double b, String operation, double expected, double actual, boolean passed, String message) {
        this.a = a;
        this.b = b;
        this.operation = operation;
        this.expected = expected;
        this.actual = actual;
        this.passed = passed;
        this.status = passed ? "PASS" : "FAIL";
        this.message = message;
    }

    public double getA() { return a; }
    public void setA(double a) { this.a = a; }

    public double getB() { return b; }
    public void setB(double b) { this.b = b; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public double getExpected() { return expected; }
    public void setExpected(double expected) { this.expected = expected; }

    public double getActual() { return actual; }
    public void setActual(double actual) { this.actual = actual; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
