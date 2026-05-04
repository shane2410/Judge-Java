package com.app.entity;

import java.sql.Timestamp;

public class TestCase {
    private int id;
    private int problemId;
    private String inputData;
    private String expectedOutput;
    private boolean isHidden;
    private int strengthScore;
    private Timestamp createdAt;

    public TestCase() {
    }

    public TestCase(int id, int problemId, String inputData, String expectedOutput, boolean isHidden, int strengthScore, Timestamp createdAt) {
        this.id = id;
        this.problemId = problemId;
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.isHidden = isHidden;
        this.strengthScore = strengthScore;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }
    public String getInputData() { return inputData; }
    public void setInputData(String inputData) { this.inputData = inputData; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public boolean isHidden() { return isHidden; }
    public void setHidden(boolean hidden) { isHidden = hidden; }
    public int getStrengthScore() { return strengthScore; }
    public void setStrengthScore(int strengthScore) { this.strengthScore = strengthScore; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
