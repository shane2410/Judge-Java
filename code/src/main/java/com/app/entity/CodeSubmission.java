package com.app.entity;

import java.sql.Timestamp;

public class CodeSubmission {
    private int id;
    private int problemId;
    private String sourceCode;
    private String language;
    private String expectedVerdict;
    private Timestamp createdAt;

    public CodeSubmission() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }
    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getExpectedVerdict() { return expectedVerdict; }
    public void setExpectedVerdict(String expectedVerdict) { this.expectedVerdict = expectedVerdict; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
