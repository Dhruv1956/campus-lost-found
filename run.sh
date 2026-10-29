#!/bin/bash
# Starts the Campus Lost & Found portal (Mac / Linux).
# Needs: Java 17+ and Maven.   Open http://localhost:8080/ afterwards.
cd "$(dirname "$0")"
mvn -q compile exec:java
