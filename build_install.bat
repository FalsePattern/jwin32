@echo off
echo Starting maven build
mvn clean compile exec:exec compile install
echo Build complete