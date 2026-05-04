package com.app.entity;

import java.sql.Timestamp;

public class EvaluationResult {
    private int id;
    private int submissionId;
    private int testcaseId;
    private String status;
    private Integer executionTimeMs;
    private Integer memoryUsedKb;
    private String actualOutput;
    private String errorMessage;
    private Timestamp evaluatedAt;

    public EvaluationResult() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }
    public int getTestcaseId() { return testcaseId; }
    public void setTestcaseId(int testcaseId) { this.testcaseId = testcaseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Integer executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public Integer getMemoryUsedKb() { return memoryUsedKb; }
    public void setMemoryUsedKb(Integer memoryUsedKb) { this.memoryUsedKb = memoryUsedKb; }
    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Timestamp getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Timestamp evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
