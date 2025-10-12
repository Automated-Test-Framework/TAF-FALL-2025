// Sélection de la DB
db = db.getSiblingDB("taf");

// --- Projects ---
db.projects.insertMany([
    {
        "_id": "project1",
        "name": "Project Alpha",
        "description": "Project Alpha Description"
    },
    {
        "_id": "project2",
        "name": "Project Beta",
        "description": "Project Beta Description"
    }
]);

// --- Test Suites ---
db.test_suites.insertMany([
    {
        "_id": "suite1",
        "projectId": "project1",
        "name": "Login Tests",
        "description": "Suite for login functionality"
    },
    {
        "_id": "suite2",
        "projectId": "project1",
        "name": "Signup Tests",
        "description": "Suite for signup functionality"
    },
    {
        "_id": "suite3",
        "projectId": "project2",
        "name": "Profile Tests",
        "description": "Suite for profile management"
    }
]);

// --- Test Cases ---
db.test_cases.insertMany([
    { "_id": "tc1", "testSuiteId": "suite1", "name": "Valid Login" },
    { "_id": "tc2", "testSuiteId": "suite1", "name": "Invalid Login" },
    { "_id": "tc3", "testSuiteId": "suite2", "name": "Valid Signup" },
    { "_id": "tc4", "testSuiteId": "suite2", "name": "Invalid Signup" },
    { "_id": "tc5", "testSuiteId": "suite3", "name": "Edit Profile" },
    { "_id": "tc6", "testSuiteId": "suite3", "name": "Delete Profile" }
]);

// --- Test Runs ---
db.test_runs.insertMany([
    {
        "_id": "tr1",
        "testSuiteId": "suite1",
        "name": "Login Run 1",
        "testCaseIds": ["tc1", "tc2"]
    },
    {
        "_id": "tr2",
        "testSuiteId": "suite2",
        "name": "Signup Run 1",
        "testCaseIds": ["tc3", "tc4"]
    },
    {
        "_id": "tr3",
        "testSuiteId": "suite3",
        "name": "Profile Run 1",
        "testCaseIds": ["tc5", "tc6"]
    }
]);

// --- Test Results ---
db.test_results.insertMany([
    { "_id": "result1", "testRunId": "tr1", "testCaseId": "tc1", "status": "PASSED" },
    { "_id": "result2", "testRunId": "tr1", "testCaseId": "tc2", "status": "FAILED" },
    { "_id": "result3", "testRunId": "tr2", "testCaseId": "tc3", "status": "PASSED" },
    { "_id": "result4", "testRunId": "tr2", "testCaseId": "tc4", "status": "BLOCKED" },
    { "_id": "result5", "testRunId": "tr3", "testCaseId": "tc5", "status": "PASSED" },
    { "_id": "result6", "testRunId": "tr3", "testCaseId": "tc6", "status": "RETEST" }
]);

print("✅ Initial data imported successfully!");
