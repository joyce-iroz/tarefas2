package com.example.tarefas;

public class Task {
    private int id;
    private String date;
    private String time;
    private String description;
    private boolean isCompleted;

    public Task(int id, String date, String time, String description) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.description = description;
        this.isCompleted = false; // Default to not completed
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}