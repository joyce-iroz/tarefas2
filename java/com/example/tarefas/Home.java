package com.example.tarefas;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Home extends AppCompatActivity implements TaskGroupAdaptativo.OnGroupClickListener {

    private List<TaskGrupo> taskGroups;
    private TaskGroupAdaptativo adapter;
    private baseDados baseDados;
    private SimpleDateFormat dateFormat, timeFormat;

    private TextView tvPendingTasks, tvTotalCompleted, tvProgressPercentage;
    private ProgressBar progressBar;
    private RecyclerView recyclerTaskGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa a base de dados e os formatadores de data/hora
        baseDados = new baseDados(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", new Locale("pt", "BR"));

        // Liga os componentes visuais do layout
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        tvTotalCompleted = findViewById(R.id.tvTotalCompleted);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        progressBar = findViewById(R.id.progress_bar);
        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);
        recyclerTaskGroups = findViewById(R.id.recyclerTaskGroups);

        // Botão para abrir a lista completa de tarefas
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Tarefas.class);
            startActivity(intent);
        });

        btnAddTask.setOnClickListener(v -> showAddTaskDialog());

        // Configura a RecyclerView e o Adaptador
        recyclerTaskGroups.setLayoutManager(new GridLayoutManager(this, 2));
        taskGroups = new ArrayList<>();
        adapter = new TaskGroupAdaptativo(taskGroups, this);
        recyclerTaskGroups.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }


    private void updateUI() {
        updateHeaderAndProgress();
        updateRecyclerView();
    }

    //Atualiza o cabeçalho e a barra de progresso.

    private void updateHeaderAndProgress() {
        String today = dateFormat.format(Calendar.getInstance().getTime());

        int pendingCount = baseDados.getTasksCount(today, false);
        tvPendingTasks.setText(String.format(Locale.getDefault(), "%d atividades pendentes", pendingCount));

        int totalToday = baseDados.getTasksCount(today, null);
        int completedToday = baseDados.getTasksCount(today, true);
        int totalCompleted = baseDados.getTotalCompletedTasks();

        int progressPercentage = 0;
        if (totalToday > 0) {
            progressPercentage = (int) (((double) completedToday / totalToday) * 100);
        }

        progressBar.setProgress(progressPercentage);
        tvProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", progressPercentage));
        tvTotalCompleted.setText(String.format(Locale.getDefault(), "%d+ atividades feitas", totalCompleted));
    }

    //Atualiza a lista de tarefas do dia na RecyclerView.

    private void updateRecyclerView() {
        String today = dateFormat.format(Calendar.getInstance().getTime());
        List<Task> tasksFromDb = baseDados.getTasksByDate(today);

        taskGroups.clear();
        taskGroups.addAll(tasksFromDb.stream()
                .map(task -> new TaskGrupo(task.getId(), task.getDescription(), task.getTime(), "1 tarefa", task.isCompleted()))
                .collect(Collectors.toList()));
        adapter.notifyDataSetChanged();
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        TextInputEditText etTaskDate = dialogView.findViewById(R.id.etTaskDate);
        TextInputEditText etTaskTime = dialogView.findViewById(R.id.etTaskTime);
        TextInputEditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        AlertDialog dialog = builder.create();
        etTaskDate.setText(dateFormat.format(Calendar.getInstance().getTime()));

        etTaskTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    Home.this,
                    (view, hourOfDay, minute) -> {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute < 30 ? 0 : 30);
                        String timeStr = timeFormat.format(c.getTime());
                        etTaskTime.setText(timeStr);
                    },
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String date = etTaskDate.getText().toString().trim();
            String time = etTaskTime.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();

            if (date.isEmpty() || time.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Task newTask = new Task(0, date, time, description);
            baseDados.addTask(newTask);
            updateUI();
            Toast.makeText(this, "Tarefa adicionada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Evento disparado quando o usuário clica em um grupo de tarefas.
     * Marca ou desmarca o status de concluído e atualiza a tela.
     */
    @Override
    public void onGroupClick(int position) {
        TaskGrupo clickedGroup = taskGroups.get(position);
        int taskId = clickedGroup.getId();
        boolean newStatus = !clickedGroup.isCompleted();

        baseDados.updateTaskCompletionStatus(taskId, newStatus);

        clickedGroup.setCompleted(newStatus);
        adapter.notifyItemChanged(position);

        updateHeaderAndProgress();
    }
}
