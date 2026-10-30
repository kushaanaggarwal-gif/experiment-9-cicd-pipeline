@echo off
echo ==============================================================================
echo Experiment 9: CI/CD Pipeline with Testing and Deployment
echo ==============================================================================
set MAVEN_CMD=C:\Users\Kushaan\AppData\Local\apache-maven-3.9.6\bin\mvn.cmd

if exist "%MAVEN_CMD%" (
    echo Using Maven: %MAVEN_CMD%
    "%MAVEN_CMD%" spring-boot:run
) else (
    echo Using system Maven...
    mvn spring-boot:run
)
pause
