package com.college.cicd.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class GitRepositoryService {

    private final File repoDir = new File("L:/FULL STACK/experiment-9-cicd-pipeline");

    public Map<String, Object> getRepositoryStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("repositoryName", "experiment-9-cicd-pipeline");
        status.put("repoPath", repoDir.getAbsolutePath());

        // Get branch
        String branch = executeGitCommand("git", "branch", "--show-current");
        status.put("branch", branch != null && !branch.isEmpty() ? branch.trim() : "main");

        // Get latest commit hash
        String commitHash = executeGitCommand("git", "rev-parse", "HEAD");
        status.put("commitHash", commitHash != null && !commitHash.isEmpty() ? commitHash.trim() : "Initial Commit");
        status.put("shortHash", commitHash != null && commitHash.length() >= 7 ? commitHash.substring(0, 7) : "HEAD");

        // Get commit message
        String commitMsg = executeGitCommand("git", "log", "-1", "--pretty=%B");
        status.put("commitMessage", commitMsg != null && !commitMsg.isEmpty() ? commitMsg.trim() : "feat: initial commit for CI/CD pipeline");

        // Get author
        String author = executeGitCommand("git", "log", "-1", "--pretty=%an <%ae>");
        status.put("author", author != null && !author.isEmpty() ? author.trim() : "Student Developer <student@college.edu>");

        // Get commit list
        List<Map<String, String>> commitList = new ArrayList<>();
        String logOutput = executeGitCommand("git", "log", "-n", "6", "--pretty=format:%h|%an|%ar|%s");
        if (logOutput != null && !logOutput.isEmpty()) {
            String[] lines = logOutput.split("\\r?\\n");
            for (String line : lines) {
                String[] parts = line.split("\\|", 4);
                if (parts.length == 4) {
                    Map<String, String> c = new LinkedHashMap<>();
                    c.put("hash", parts[0]);
                    c.put("author", parts[1]);
                    c.put("timeAgo", parts[2]);
                    c.put("message", parts[3]);
                    commitList.add(c);
                }
            }
        }
        status.put("recentCommits", commitList);
        status.put("isRepositoryConfigured", true);
        return status;
    }

    private String executeGitCommand(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(repoDir);
            Process proc = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            proc.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }
}
