package com.college.cicd.service;

import com.college.cicd.model.CalculatorResult;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    public double add(double a, double b) {
        return a + b;
    }

    public double subtract(double a, double b) {
        return a - b;
    }

    public double multiply(double a, double b) {
        return a * b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero is not permitted.");
        }
        return a / b;
    }

    public CalculatorResult testAddition(double a, double b, double expected) {
        double actual = add(a, b);
        boolean passed = Math.abs(actual - expected) < 0.0001;
        String msg = passed 
            ? String.format("Assertion passed: add(%.1f, %.1f) == %.1f", a, b, expected)
            : String.format("Assertion failed: expected %.1f but got %.1f", expected, actual);
        return new CalculatorResult(a, b, "ADD", expected, actual, passed, msg);
    }
}
