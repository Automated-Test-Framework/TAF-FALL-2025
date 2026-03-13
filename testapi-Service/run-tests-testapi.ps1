# One-click unit test runner for testapi-Service backend
# Usage: .\testapi-Service\run-tests-testapi.ps1 (from any directory)

mvn test -f "$PSScriptRoot\pom.xml" -pl backend -am
