package com.example.tarefas;

public class TaskGroup {
    private int id; // ID da tarefa original
    private String title;
    private String time;
    private String taskCount;
    private boolean isCompleted;

    public TaskGroup(int id, String title, String time, String taskCount, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.taskCount = taskCount;
        this.isCompleted = isCompleted;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getTaskCount() { return taskCount; }
    public boolean isCompleted() { return isCompleted; }

    // Setter
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
