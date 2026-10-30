package com.college.cicd.model;

public class PostgresContainerConfig {
    private String dockerImage = "postgres:15-alpine";
    private String databaseName = "testdb";
    private String username = "postgres";
    private String password = "secret_password";
    private int containerPort = 5432;

    public PostgresContainerConfig() {}

    public PostgresContainerConfig(String dockerImage, String databaseName, String username, String password, int containerPort) {
        this.dockerImage = dockerImage;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.containerPort = containerPort;
    }

    public String getDockerImage() { return dockerImage; }
    public String getDatabaseName() { return databaseName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getContainerPort() { return containerPort; }
}
