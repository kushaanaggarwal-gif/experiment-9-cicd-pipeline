package com.college.cicd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HealthAndVersionControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Health Probe: GET /api/health returns HTTP 200 OK with UP status")
    public void testHealthProbeReturns200Ok() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.httpStatusText").value("HTTP 200 OK"))
                .andExpect(jsonPath("$.components.httpListener.status").value("UP"))
                .andExpect(jsonPath("$.components.database.status").value("UP"));
    }

    @Test
    @DisplayName("Docker Inspect: GET /api/docker/inspect returns container layers and config")
    public void testDockerInspectReturnsDetails() throws Exception {
        mockMvc.perform(get("/api/docker/inspect").param("tag", "v2.0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Id", notNullValue()))
                .andExpect(jsonPath("$.RepoTags", hasItem("experiment-9-app:v2.0.0")))
                .andExpect(jsonPath("$.Config.User").value("cicduser"))
                .andExpect(jsonPath("$.Config.Healthcheck.Test", hasItem("CMD-SHELL")))
                .andExpect(jsonPath("$.LayerBreakdown", hasSize(5)));
    }

    @Test
    @DisplayName("Version Status: GET /api/version/status contrasts v2.0.0 and v1.0.0")
    public void testVersionStatusContrast() throws Exception {
        mockMvc.perform(get("/api/version/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentRelease").value("v2.0.0"))
                .andExpect(jsonPath("$.previousVersion").value("v1.0.0"))
                .andExpect(jsonPath("$.currentStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.defectRemediationVerified").value(true));
    }

    @Test
    @DisplayName("Version Test v2.0.0: Current release candidate returns HTTP 200 OK")
    public void testCurrentVersionReturns200() throws Exception {
        mockMvc.perform(get("/api/version/test").param("version", "v2.0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.httpStatusText").value("HTTP 200 OK"))
                .andExpect(jsonPath("$.defectStatus").value("RESOLVED"));
    }

    @Test
    @DisplayName("Version Test v1.0.0: Previous release reproduces defect with HTTP 500 Error")
    public void testPreviousVersionReturns500Defect() throws Exception {
        mockMvc.perform(get("/api/version/test").param("version", "v1.0.0"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.error").value("ConnectionPoolExhaustedException"))
                .andExpect(jsonPath("$.status").value("FAILED"));
    }
}
