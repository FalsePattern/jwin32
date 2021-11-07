@echo off
echo Starting maven build
mvn clean compile exec:exec compile package
echo Build complete