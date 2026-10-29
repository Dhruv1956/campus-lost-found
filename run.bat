@echo off
rem Starts the Campus Lost & Found portal (Windows).
rem Needs: Java 17+ and Maven.   Open http://localhost:8080/ afterwards.
cd /d "%~dp0"
mvn -q compile exec:java
