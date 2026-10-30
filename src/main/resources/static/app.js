// JavaScript for Experiment 9: CI/CD Pipeline Dashboard

const API_BASE = "";

// -------------------------------------------------------------
// Initialization
// -------------------------------------------------------------
window.addEventListener("DOMContentLoaded", () => {
  refreshOverview();
  loadProducts();
  checkDockerStatus();
  loadGitStatus();
  loadDockerImages();
  fetchCanaryStatus();
  fetchLogs();
});

// -------------------------------------------------------------
// Section A: Overview & Status
// -------------------------------------------------------------
async function refreshOverview() {
  try {
    const res = await fetch(`${API_BASE}/api/pipeline/overview`);
    const data = await res.json();

    const statusBadge = document.getElementById("pipeline-status-badge");
    const buildText = document.getElementById("build-status-text");
    const deployText = document.getElementById("deployment-status-text");

    const repBuild = document.getElementById("rep-build");
    const repTest = document.getElementById("rep-test");
    const repDeploy = document.getElementById("rep-deploy");

    statusBadge.textContent = data.pipelineStatus;
    statusBadge.className = "badge " + getBadgeClass(data.pipelineStatus);

    buildText.textContent = data.buildStatus;
    deployText.textContent = data.deploymentStatus;

    if (repBuild) repBuild.textContent = data.buildStatus;
    if (repDeploy) repDeploy.textContent = data.deploymentStatus;
    if (repTest && data.latestTestSummary) {
      repTest.textContent = `${data.latestTestSummary.overallStatus} (${data.latestTestSummary.passedTests}/${data.latestTestSummary.totalTests} passed)`;
    }
  } catch (err) {
    console.error("Failed to refresh overview:", err);
  }
}

function getBadgeClass(status) {
  if (status === "PASSED" || status === "PASS" || status === "SUCCESS" || status === "IN_STOCK" || status === "ACTIVE_IN_REGISTRY") return "badge-pass";
  if (status === "FAILED" || status === "FAIL" || status === "OUT_OF_STOCK") return "badge-fail";
  if (status === "SKIPPED" || status === "SKIP" || status === "LOW_STOCK") return "badge-skip";
  return "badge-info";
}

// -------------------------------------------------------------
// Section B: Live Application - Product Management
// -------------------------------------------------------------
async function loadProducts() {
  const search = document.getElementById("app-search-input").value;
  const category = document.getElementById("app-category-filter").value;

  let url = `${API_BASE}/api/products`;
  const params = [];
  if (search) params.push(`search=${encodeURIComponent(search)}`);
  if (category) params.push(`category=${encodeURIComponent(category)}`);
  if (params.length > 0) url += `?${params.join("&")}`;

  try {
    const res = await fetch(url);
    const products = await res.json();

    const tbody = document.getElementById("products-table-body");
    tbody.innerHTML = "";

    if (!products || products.length === 0) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--text-muted);">No products found matching criteria.</td></tr>`;
    } else {
      products.forEach(p => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td><strong>${p.id}</strong></td>
          <td><strong>${escapeHtml(p.name)}</strong></td>
          <td><code>${escapeHtml(p.sku)}</code></td>
          <td>${escapeHtml(p.category || '-')}</td>
          <td>$${(p.price || 0).toFixed(2)}</td>
          <td>${p.stockQuantity != null ? p.stockQuantity : 0}</td>
          <td><span class="badge ${getBadgeClass(p.status)}">${p.status}</span></td>
          <td>
            <button onclick="deleteProduct(${p.id})" style="padding: 2px 8px; font-size: 0.75rem; color: var(--error-color);">Delete</button>
          </td>
        `;
        tbody.appendChild(tr);
      });
    }

    loadProductStats();
  } catch (err) {
    console.error("Error loading products:", err);
  }
}

async function loadProductStats() {
  try {
    const res = await fetch(`${API_BASE}/api/products/stats`);
    const stats = await res.json();

    document.getElementById("stat-total-products").textContent = stats.totalProducts || 0;
    document.getElementById("stat-in-stock").textContent = stats.inStockCount || 0;
    document.getElementById("stat-low-stock").textContent = stats.lowStockCount || 0;
    document.getElementById("stat-total-value").textContent = "$" + (stats.totalInventoryValue || 0).toFixed(2);
  } catch (err) {
    console.error("Error loading product stats:", err);
  }
}

function resetProductFilters() {
  document.getElementById("app-search-input").value = "";
  document.getElementById("app-category-filter").value = "";
  loadProducts();
}

function toggleAddProductForm() {
  const container = document.getElementById("add-product-form-container");
  container.style.display = container.style.display === "none" ? "block" : "none";
  document.getElementById("add-product-msg").textContent = "";
}

async function submitNewProduct() {
  const name = document.getElementById("new-prod-name").value;
  const sku = document.getElementById("new-prod-sku").value;
  const category = document.getElementById("new-prod-category").value;
  const price = parseFloat(document.getElementById("new-prod-price").value);
  const stock = parseInt(document.getElementById("new-prod-stock").value);
  const desc = document.getElementById("new-prod-desc").value;
  const msgEl = document.getElementById("add-product-msg");

  if (!name || !sku) {
    msgEl.innerHTML = `<span style="color: var(--error-color);">Please provide product name and SKU.</span>`;
    return;
  }

  const payload = {
    name: name,
    sku: sku,
    category: category,
    price: price,
    stockQuantity: stock,
    description: desc
  };

  try {
    const res = await fetch(`${API_BASE}/api/products`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      document.getElementById("new-prod-name").value = "";
      document.getElementById("new-prod-sku").value = "";
      document.getElementById("new-prod-desc").value = "";
      msgEl.innerHTML = `<span style="color: var(--success-color);">Product created successfully!</span>`;
      setTimeout(() => { toggleAddProductForm(); }, 1200);
      loadProducts();
      fetchLogs();
    } else {
      const errData = await res.json();
      msgEl.innerHTML = `<span style="color: var(--error-color);">Error: ${errData.error || 'Failed to create product'}</span>`;
    }
  } catch (err) {
    msgEl.innerHTML = `<span style="color: var(--error-color);">Network error: ${err.message}</span>`;
  }
}

async function deleteProduct(id) {
  if (!confirm(`Are you sure you want to delete product ID ${id}?`)) return;
  try {
    const res = await fetch(`${API_BASE}/api/products/${id}`, { method: "DELETE" });
    if (res.ok) {
      loadProducts();
      fetchLogs();
    } else {
      alert("Failed to delete product.");
    }
  } catch (err) {
    alert("Error: " + err.message);
  }
}

function escapeHtml(str) {
  if (!str) return "";
  return str.replace(/[&<>"']/g, function(m) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
  });
}

// -------------------------------------------------------------
// Section C: Testing Module
// -------------------------------------------------------------

// 1. Unit Testing - Calculator
async function runCalculatorUnitTest() {
  const a = document.getElementById("calc-a").value || 15;
  const b = document.getElementById("calc-b").value || 25;
  const exp = document.getElementById("calc-exp").value || 40;

  try {
    const res = await fetch(`${API_BASE}/api/testing/unit/calculator?a=${a}&b=${b}&expected=${exp}`);
    const data = await res.json();

    document.getElementById("unit-test-result").style.display = "block";
    document.getElementById("unit-expected").textContent = data.expected;
    document.getElementById("unit-actual").textContent = data.actual;
    
    const badge = document.getElementById("unit-status-badge");
    badge.textContent = data.status;
    badge.className = "badge " + (data.passed ? "badge-pass" : "badge-fail");

    document.getElementById("unit-message").textContent = data.message;
    fetchLogs();
  } catch (err) {
    alert("Error executing unit test: " + err.message);
  }
}

// 2. MockMvc API Integration Test
async function runMockMvcTest() {
  try {
    const res = await fetch(`${API_BASE}/api/posts`);
    const data = await res.json();

    document.getElementById("mockmvc-result").style.display = "block";
    document.getElementById("mockmvc-endpoint").textContent = "GET /api/posts";
    document.getElementById("mockmvc-status").textContent = res.status + " " + res.statusText;
    document.getElementById("mockmvc-body").textContent = JSON.stringify(data, null, 2);
    fetchLogs();
  } catch (err) {
    alert("Error executing MockMvc request: " + err.message);
  }
}

async function runMockMvcProductTest() {
  try {
    const res = await fetch(`${API_BASE}/api/products`);
    const data = await res.json();

    document.getElementById("mockmvc-result").style.display = "block";
    document.getElementById("mockmvc-endpoint").textContent = "GET /api/products";
    document.getElementById("mockmvc-status").textContent = res.status + " " + res.statusText;
    document.getElementById("mockmvc-body").textContent = JSON.stringify(data, null, 2);
    fetchLogs();
  } catch (err) {
    alert("Error executing MockMvc product request: " + err.message);
  }
}

// 3. Pact Contract Testing
async function runPactContractTest() {
  const radios = document.getElementsByName("pactMode");
  let simulateDrift = false;
  for (const r of radios) {
    if (r.checked && r.value === "true") {
      simulateDrift = true;
      break;
    }
  }

  try {
    const res = await fetch(`${API_BASE}/api/testing/contract?simulateDrift=${simulateDrift}`);
    const data = await res.json();

    document.getElementById("pact-result").style.display = "block";
    const badge = document.getElementById("pact-status-badge");
    badge.textContent = data.contractStatus;
    badge.className = "badge " + (data.passed ? "badge-pass" : "badge-fail");

    document.getElementById("pact-summary").textContent = data.summary;

    const violationsContainer = document.getElementById("pact-violations-container");
    const violationsList = document.getElementById("pact-violations-list");
    violationsList.innerHTML = "";

    if (data.violations && data.violations.length > 0) {
      violationsContainer.style.display = "block";
      data.violations.forEach(v => {
        const li = document.createElement("li");
        li.textContent = v;
        violationsList.appendChild(li);
      });
    } else {
      violationsContainer.style.display = "none";
    }

    const comparison = {
      consumerExpectedSchema: data.expectedContract,
      providerActualPayload: data.providerResponse
    };
    document.getElementById("pact-comparison-body").textContent = JSON.stringify(comparison, null, 2);
    fetchLogs();
  } catch (err) {
    alert("Error executing Pact verification: " + err.message);
  }
}

// 4. Testcontainers & Docker Engine Status
async function checkDockerStatus() {
  try {
    const res = await fetch(`${API_BASE}/api/pipeline/docker-status`);
    const data = await res.json();

    const badge = document.getElementById("docker-engine-badge");
    const msg = document.getElementById("docker-engine-msg");
    const repDocker = document.getElementById("rep-docker");

    if (data.available) {
      badge.textContent = "AVAILABLE (" + data.version + ")";
      badge.className = "badge badge-pass";
      msg.textContent = data.message;
      if (repDocker) repDocker.textContent = "Available";
    } else {
      badge.textContent = "NOT DETECTED (Host Engine Inactive)";
      badge.className = "badge badge-skip";
      msg.textContent = data.message;
      if (repDocker) repDocker.textContent = "Not Detected";
    }
  } catch (err) {
    console.error("Error checking Docker status:", err);
  }
}

// Master Action: Run All Configured Automated Tests
async function runAutomatedTestSuite() {
  const btn = document.getElementById("btn-run-all-tests");
  const spinner = document.getElementById("test-executing-spinner");
  btn.disabled = true;
  spinner.style.display = "inline";

  try {
    const res = await fetch(`${API_BASE}/api/pipeline/run-tests`, { method: "POST" });
    const summary = await res.json();

    document.getElementById("test-suite-summary-box").style.display = "block";
    document.getElementById("summary-total").textContent = summary.totalTests;
    document.getElementById("summary-passed").textContent = summary.passedTests;
    document.getElementById("summary-failed").textContent = summary.failedTests;
    document.getElementById("summary-skipped").textContent = summary.skippedTests;
    document.getElementById("summary-duration").textContent = summary.totalDurationMs;

    const overallBadge = document.getElementById("summary-overall-badge");
    overallBadge.textContent = summary.overallStatus;
    overallBadge.className = "badge " + getBadgeClass(summary.overallStatus);

    const tbody = document.getElementById("test-results-tbody");
    tbody.innerHTML = "";

    summary.results.forEach(test => {
      const tr = document.createElement("tr");

      const tdName = document.createElement("td");
      tdName.innerHTML = `<code>${test.testName}</code>`;

      const tdCat = document.createElement("td");
      tdCat.textContent = test.category;

      const tdStatus = document.createElement("td");
      const badge = document.createElement("span");
      badge.className = "badge " + getBadgeClass(test.status);
      badge.textContent = test.status;
      tdStatus.appendChild(badge);

      const tdDur = document.createElement("td");
      tdDur.textContent = test.durationMs + " ms";

      const tdDetails = document.createElement("td");
      if (test.status === "FAIL") {
        tdDetails.innerHTML = `<span style="color:var(--error-color); font-weight:600;">${test.errorDetails || 'Failed assertion'}</span>`;
      } else if (test.status === "SKIPPED") {
        tdDetails.innerHTML = `<span style="color:var(--warning-color);">${test.errorDetails || 'Skipped per environment specification'}</span>`;
      } else {
        tdDetails.textContent = test.description || "Passed";
      }

      tr.appendChild(tdName);
      tr.appendChild(tdCat);
      tr.appendChild(tdStatus);
      tr.appendChild(tdDur);
      tr.appendChild(tdDetails);
      tbody.appendChild(tr);
    });

    refreshOverview();
    fetchLogs();
  } catch (err) {
    alert("Error executing test suite: " + err.message);
  } finally {
    btn.disabled = false;
    spinner.style.display = "none";
  }
}

// -------------------------------------------------------------
// Section D: Git Repository & CI Pipeline
// -------------------------------------------------------------
async function loadGitStatus() {
  try {
    const res = await fetch(`${API_BASE}/api/git/status`);
    const data = await res.json();

    document.getElementById("git-repo-name").textContent = data.repositoryName;
    document.getElementById("git-branch").textContent = data.branch;
    document.getElementById("git-commit-hash").textContent = data.shortHash || "c1e4fa8";
    document.getElementById("git-author").textContent = data.author;
    document.getElementById("git-commit-msg").textContent = data.commitMessage;

    const tbody = document.getElementById("git-commits-tbody");
    tbody.innerHTML = "";

    if (data.recentCommits && data.recentCommits.length > 0) {
      data.recentCommits.forEach(c => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td><code>${c.hash}</code></td>
          <td>${escapeHtml(c.author)}</td>
          <td>${escapeHtml(c.timeAgo)}</td>
          <td>${escapeHtml(c.message)}</td>
        `;
        tbody.appendChild(tr);
      });
    } else {
      tbody.innerHTML = `<tr><td colspan="4">No commits found</td></tr>`;
    }
  } catch (err) {
    console.error("Error loading git status:", err);
  }
}

async function simulateGitHubActionsCI() {
  const container = document.getElementById("ci-runner-simulation");
  const stepsDiv = document.getElementById("ci-steps-container");
  container.style.display = "block";
  stepsDiv.innerHTML = "<p>Initializing GitHub Actions Runner (ubuntu-latest)...</p>";

  const stages = [
    { name: "Set up job (ubuntu-latest, runner v2.311.0)", delay: 300 },
    { name: "actions/checkout@v4 (checking out commit c1e4fa8 on main)", delay: 400 },
    { name: "actions/setup-java@v4 (Eclipse Temurin JDK 17)", delay: 500 },
    { name: "Run mvn compile --batch-mode", delay: 700 },
    { name: "Run mvn test --batch-mode (8 Automated Tests)", delay: 900 },
    { name: "Package Spring Boot executable JAR (mvn package -DskipTests)", delay: 600 },
    { name: "actions/upload-artifact@v4 (surefire-test-reports)", delay: 400 },
    { name: "Post Run actions/checkout@v4 & Complete Job", delay: 300 }
  ];

  stepsDiv.innerHTML = "";
  for (let i = 0; i < stages.length; i++) {
    const stage = stages[i];
    const stepEl = document.createElement("div");
    stepEl.style.padding = "6px 10px";
    stepEl.style.margin = "4px 0";
    stepEl.style.border = "1px solid var(--border-color)";
    stepEl.style.backgroundColor = "#ffffff";
    stepEl.style.display = "flex";
    stepEl.style.justifyContent = "space-between";
    stepEl.innerHTML = `<span><strong>Step ${i + 1}:</strong> ${stage.name}</span> <span class="badge badge-info">RUNNING</span>`;
    stepsDiv.appendChild(stepEl);

    await new Promise(r => setTimeout(r, stage.delay));
    stepEl.querySelector(".badge").className = "badge badge-pass";
    stepEl.querySelector(".badge").textContent = "SUCCESS";
  }

  // Trigger test runner in background to refresh summary
  runAutomatedTestSuite();
}

async function runLocalBuild() {
  try {
    const res = await fetch(`${API_BASE}/api/pipeline/run-build`, { method: "POST" });
    const data = await res.json();

    document.getElementById("build-result-box").style.display = "block";
    const badge = document.getElementById("local-build-status");
    badge.textContent = data.status;
    badge.className = "badge " + (data.status === "SUCCESS" ? "badge-pass" : "badge-fail");

    document.getElementById("local-build-msg").textContent = data.message;
    document.getElementById("local-build-output").textContent = data.output || "";

    refreshOverview();
    fetchLogs();
  } catch (err) {
    alert("Error executing local build: " + err.message);
  }
}

// -------------------------------------------------------------
// Section E: Docker and Container Registry
// -------------------------------------------------------------
async function loadDockerImages() {
  try {
    const res = await fetch(`${API_BASE}/api/docker/images`);
    const images = await res.json();

    const tbody = document.getElementById("docker-images-tbody");
    tbody.innerHTML = "";

    if (!images || images.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7">No images in local registry catalog.</td></tr>`;
      return;
    }

    images.forEach(img => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><strong>${escapeHtml(img.repository)}</strong></td>
        <td><code>${escapeHtml(img.tag)}</code></td>
        <td><code>${img.id.substring(0, 19)}...</code></td>
        <td>${img.size}</td>
        <td>${escapeHtml(img.baseImage)}</td>
        <td>${img.created}</td>
        <td><span class="badge ${getBadgeClass(img.status)}">${img.status}</span></td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("Error loading docker images:", err);
  }
}

async function triggerDockerBuild() {
  const resultBox = document.getElementById("docker-build-result");
  const badge = document.getElementById("docker-build-badge");
  const text = document.getElementById("docker-build-text");
  const layersDiv = document.getElementById("docker-layers-list");

  badge.textContent = "BUILDING...";
  badge.className = "badge badge-info";
  resultBox.style.display = "block";
  text.textContent = "Packaging Spring Boot JAR and registering Docker image in container catalog...";
  layersDiv.innerHTML = "";

  try {
    const res = await fetch(`${API_BASE}/api/pipeline/docker-build?tag=v2.0.0`, { method: "POST" });
    const data = await res.json();

    badge.textContent = data.success ? "SUCCESS" : "FAILED";
    badge.className = "badge " + (data.success ? "badge-pass" : "badge-fail");
    text.textContent = data.message;

    if (data.layers) {
      layersDiv.innerHTML = "<strong>Multi-Stage Build Layer Verification:</strong>";
      const ul = document.createElement("ul");
      ul.style.marginTop = "6px";
      data.layers.forEach(l => {
        const li = document.createElement("li");
        li.innerHTML = `<code>${escapeHtml(l)}</code>`;
        ul.appendChild(li);
      });
      layersDiv.appendChild(ul);
    }

    loadDockerImages();
    fetchLogs();
  } catch (err) {
    badge.textContent = "ERROR";
    badge.className = "badge badge-fail";
    text.textContent = "Build failed: " + err.message;
  }
}

// -------------------------------------------------------------
// Section F: Canary Deployment
// -------------------------------------------------------------
async function fetchCanaryStatus() {
  try {
    const res = await fetch(`${API_BASE}/api/canary/status`);
    const data = await res.json();
    updateCanaryUI(data);
  } catch (err) {
    console.error("Error fetching canary status:", err);
  }
}

function updateCanaryUI(data) {
  const canaryPct = data.rolloutPercentage;
  const stablePct = 100 - canaryPct;

  const stableBar = document.getElementById("traffic-stable-bar");
  const canaryBar = document.getElementById("traffic-canary-bar");

  stableBar.style.width = stablePct + "%";
  stableBar.textContent = `Stable v1: ${stablePct}%`;

  canaryBar.style.width = canaryPct + "%";
  canaryBar.textContent = canaryPct > 0 ? `Canary v2: ${canaryPct}%` : "";

  document.getElementById("canary-percent-text").textContent = canaryPct + "%";

  document.getElementById("cnt-total").textContent = data.totalRequests;
  document.getElementById("cnt-stable").textContent = data.stableRequests;
  document.getElementById("cnt-canary").textContent = data.canaryRequests;

  if (data.simulatedHealthMetrics) {
    const hm = data.simulatedHealthMetrics;
    const badge = document.getElementById("health-status-badge");
    badge.textContent = hm.status;
    badge.className = "badge " + (hm.status.includes("HEALTHY") ? "badge-pass" : "badge-info");
    document.getElementById("health-error-rate").textContent = hm.errorRate;
    document.getElementById("health-latency").textContent = hm.p95Latency;
  }
}

async function setCanaryPercentage(pct) {
  try {
    const res = await fetch(`${API_BASE}/api/canary/set-rollout?percentage=${pct}`, { method: "POST" });
    const data = await res.json();
    updateCanaryUI(data);
    fetchLogs();
  } catch (err) {
    alert("Error setting canary percentage: " + err.message);
  }
}

async function rollbackCanary() {
  try {
    const res = await fetch(`${API_BASE}/api/canary/rollback`, { method: "POST" });
    const data = await res.json();
    updateCanaryUI(data);
    fetchLogs();
  } catch (err) {
    alert("Error rolling back canary: " + err.message);
  }
}

async function resetCanaryCounters() {
  try {
    const res = await fetch(`${API_BASE}/api/canary/reset-counters`, { method: "POST" });
    const data = await res.json();
    updateCanaryUI(data);
    document.getElementById("canary-traces-tbody").innerHTML = "";
    document.getElementById("canary-routing-log").style.display = "none";
  } catch (err) {
    alert("Error resetting counters: " + err.message);
  }
}

async function sendCanarySingleRequest() {
  try {
    const res = await fetch(`${API_BASE}/api/canary/route`);
    const trace = await res.json();
    appendCanaryTrace(trace);
    fetchCanaryStatus();
  } catch (err) {
    alert("Error sending request: " + err.message);
  }
}

async function sendCanaryBatchRequests(count) {
  try {
    const res = await fetch(`${API_BASE}/api/canary/batch-route?count=${count}`, { method: "POST" });
    const batch = await res.json();

    if (batch.traces) {
      batch.traces.forEach(t => appendCanaryTrace(t));
    }
    fetchCanaryStatus();
  } catch (err) {
    alert("Error sending batch requests: " + err.message);
  }
}

function appendCanaryTrace(trace) {
  document.getElementById("canary-routing-log").style.display = "block";
  const tbody = document.getElementById("canary-traces-tbody");

  const tr = document.createElement("tr");
  tr.innerHTML = `
    <td>${trace.requestId}</td>
    <td>${trace.rollValue} (Threshold: &lt; ${trace.rolloutThreshold})</td>
    <td>${trace.routedVersion}</td>
    <td><span class="badge ${trace.releaseType === 'CANARY' ? 'badge-pass' : 'badge-info'}">${trace.releaseType}</span></td>
    <td>${trace.latencyMs} ms</td>
  `;

  tbody.insertBefore(tr, tbody.firstChild);

  while (tbody.children.length > 20) {
    tbody.removeChild(tbody.lastChild);
  }
}

// -------------------------------------------------------------
// Section G: Execution Logs
// -------------------------------------------------------------
async function fetchLogs() {
  try {
    const res = await fetch(`${API_BASE}/api/pipeline/logs`);
    const logs = await res.json();

    const consoleDiv = document.getElementById("logs-console");
    consoleDiv.innerHTML = "";

    logs.forEach(log => {
      const entry = document.createElement("div");
      entry.className = "log-entry";
      entry.textContent = log;
      consoleDiv.appendChild(entry);
    });

    consoleDiv.scrollTop = consoleDiv.scrollHeight;
  } catch (err) {
    console.error("Error fetching logs:", err);
  }
}

async function clearLogs() {
  try {
    await fetch(`${API_BASE}/api/pipeline/logs/clear`, { method: "POST" });
    fetchLogs();
  } catch (err) {
    alert("Error clearing logs: " + err.message);
  }
}
