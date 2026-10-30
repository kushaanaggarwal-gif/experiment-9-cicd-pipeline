package com.college.cicd.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContractVerificationService {

    public Map<String, Object> getExpectedConsumerContract() {
        Map<String, Object> contract = new LinkedHashMap<>();
        contract.put("consumer", "FrontendWebClient");
        contract.put("provider", "PostManagementService");
        contract.put("endpoint", "GET /api/posts/1");
        contract.put("expectedStatus", 200);

        Map<String, String> expectedFields = new LinkedHashMap<>();
        expectedFields.put("id", "Number (e.g. 1)");
        expectedFields.put("title", "String (e.g. 'CI/CD Best Practices')");
        expectedFields.put("content", "String (e.g. 'Automated testing reduces regression risk.')");
        expectedFields.put("author", "String (e.g. 'DevOps Team')");
        contract.put("expectedBodySchema", expectedFields);

        return contract;
    }

    public Map<String, Object> verifyContract(boolean simulateSchemaDrift) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("consumer", "FrontendWebClient");
        response.put("provider", "PostManagementService");
        response.put("endpoint", "GET /api/posts/1");

        Map<String, Object> expectedSchema = new LinkedHashMap<>();
        expectedSchema.put("id", "Long/Integer");
        expectedSchema.put("title", "String");
        expectedSchema.put("content", "String");
        expectedSchema.put("author", "String");
        response.put("expectedContract", expectedSchema);

        Map<String, Object> providerPayload = new LinkedHashMap<>();
        List<String> violations = new ArrayList<>();

        if (simulateSchemaDrift) {
            // Breaking schema drift: field names altered by provider team without contract renegotiation
            providerPayload.put("id", 1);
            providerPayload.put("heading", "CI/CD Best Practices"); // Renamed from title
            providerPayload.put("body_text", "Automated testing reduces regression risk."); // Renamed from content
            providerPayload.put("author_name", "DevOps Team"); // Renamed from author

            violations.add("Missing expected field: 'title' (Provider returned 'heading')");
            violations.add("Missing expected field: 'content' (Provider returned 'body_text')");
            violations.add("Missing expected field: 'author' (Provider returned 'author_name')");

            response.put("providerResponse", providerPayload);
            response.put("contractStatus", "FAIL");
            response.put("passed", false);
            response.put("violations", violations);
            response.put("summary", "Contract verification failed: Provider schema violates Consumer expectations. Breaking change prevented by Pact contract test!");
        } else {
            // Compliant response matching contract exactly
            providerPayload.put("id", 1);
            providerPayload.put("title", "CI/CD Best Practices");
            providerPayload.put("content", "Automated testing reduces regression risk.");
            providerPayload.put("author", "DevOps Team");

            response.put("providerResponse", providerPayload);
            response.put("contractStatus", "PASS");
            response.put("passed", true);
            response.put("violations", Collections.emptyList());
            response.put("summary", "Contract verification passed: Provider response strictly satisfies the consumer Pact specification.");
        }

        return response;
    }
}
