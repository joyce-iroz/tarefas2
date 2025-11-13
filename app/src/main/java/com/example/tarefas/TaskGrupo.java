package com.example.tarefas;

public class TaskGrupo {
    private int id; // ID do grupo de tarefas
    private String title; // Título do grupo
    private String time; // Horário ou data relacionada
    private String taskCount; // Quantidade de tarefas no grupo
    private boolean isCompleted; // Indica se o grupo está concluído

    /**
     * Construtor do grupo de tarefas.
     * @param id identificador do grupo
     * @param title título do grupo
     * @param time horário ou tempo associado
     * @param taskCount quantidade de tarefas
     * @param isCompleted status de conclusão
     */
    public TaskGrupo(int id, String title, String time, String taskCount, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.taskCount = taskCount;
        this.isCompleted = isCompleted;
    }


    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
    public String getTaskCount() { return taskCount; }
    public boolean isCompleted() { return isCompleted; }

    // Setter
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
