package com.college.cicd;

import com.college.cicd.service.ContractVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Contract Tests: Pact Consumer-Provider Verification")
public class PactContractTest {

    private ContractVerificationService contractService;

    @BeforeEach
    void setUp() {
        contractService = new ContractVerificationService();
    }

    @Test
    @DisplayName("Contract Test 1: Verify Provider satisfies Consumer Pact contract")
    void testProviderMatchesContract() {
        Map<String, Object> result = contractService.verifyContract(false);
        assertEquals("PASS", result.get("contractStatus"), "Contract status should be PASS");
        assertTrue((Boolean) result.get("passed"), "Contract verification should pass");

        @SuppressWarnings("unchecked")
        List<String> violations = (List<String>) result.get("violations");
        assertTrue(violations.isEmpty(), "There should be no schema violations");
    }

    @Test
    @DisplayName("Contract Test 2: Detect Breaking Schema Drift (Expected Failure)")
    void testSchemaDriftCausesFailure() {
        // When provider team renames fields without updating contract
        Map<String, Object> result = contractService.verifyContract(true);
        assertEquals("FAIL", result.get("contractStatus"), "Contract status should be FAIL due to schema drift");
        assertFalse((Boolean) result.get("passed"), "Verification should fail when contract broken");

        @SuppressWarnings("unchecked")
        List<String> violations = (List<String>) result.get("violations");
        assertFalse(violations.isEmpty(), "Schema drift should produce violations");
        assertTrue(violations.stream().anyMatch(v -> v.contains("title")), "Should detect missing title field");
    }
}
