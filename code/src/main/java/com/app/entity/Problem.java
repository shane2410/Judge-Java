package com.app.entity;

import java.sql.Timestamp;

public class Problem {
    private int id;
    private String title;
    private String description;
    private String imagePath;
    private int timeLimitMs;
    private int memoryLimitKb;
    private Timestamp createdAt;

    public Problem() {
    }

    public Problem(int id, String title, String description, String imagePath, int timeLimitMs, int memoryLimitKb, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imagePath = imagePath;
        this.timeLimitMs = timeLimitMs;
        this.memoryLimitKb = memoryLimitKb;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public int getTimeLimitMs() { return timeLimitMs; }
    public void setTimeLimitMs(int timeLimitMs) { this.timeLimitMs = timeLimitMs; }
    public int getMemoryLimitKb() { return memoryLimitKb; }
    public void setMemoryLimitKb(int memoryLimitKb) { this.memoryLimitKb = memoryLimitKb; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
