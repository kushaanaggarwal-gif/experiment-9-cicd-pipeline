package com.college.cicd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Exp9CicdApplication {

    public static void main(String[] args) {
        SpringApplication.run(Exp9CicdApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" Experiment 9: CI/CD Pipeline with Testing & Deployment");
        System.out.println(" Dashboard running at: http://localhost:8089");
        System.out.println("==================================================================");
    }
}
