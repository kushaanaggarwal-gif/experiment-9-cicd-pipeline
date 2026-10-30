package com.college.cicd;

import com.college.cicd.service.CalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: CalculatorService (JUnit 5)")
public class CalculatorServiceTest {

    private CalculatorService calculator;

    @BeforeEach
    void setUp() {
        calculator = new CalculatorService();
    }

    @Test
    @DisplayName("Unit Test 1: Addition of two positive numbers")
    void testAdditionPositive() {
        double result = calculator.add(10.5, 5.5);
        assertEquals(16.0, result, 0.0001, "10.5 + 5.5 should equal 16.0");
    }

    @Test
    @DisplayName("Unit Test 2: Subtraction of numbers")
    void testSubtraction() {
        double result = calculator.subtract(20.0, 7.5);
        assertEquals(12.5, result, 0.0001, "20.0 - 7.5 should equal 12.5");
    }

    @Test
    @DisplayName("Unit Test 3: Multiplication of numbers")
    void testMultiplication() {
        double result = calculator.multiply(4.0, 2.5);
        assertEquals(10.0, result, 0.0001, "4.0 * 2.5 should equal 10.0");
    }

    @Test
    @DisplayName("Unit Test 4: Division of numbers")
    void testDivision() {
        double result = calculator.divide(15.0, 3.0);
        assertEquals(5.0, result, 0.0001, "15.0 / 3.0 should equal 5.0");
    }

    @Test
    @DisplayName("Unit Test 5: Division by zero throws IllegalArgumentException")
    void testDivisionByZeroThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculator.divide(10.0, 0.0);
        });
        assertTrue(exception.getMessage().contains("Division by zero is not permitted"));
    }
}
