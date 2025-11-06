package com.example.tarefas;

import android.app.DatePickerDialog;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements TaskGroupAdapter.OnGroupClickListener {

    private List<TaskGroup> taskGroups;
    private TaskGroupAdapter adapter;
    private DatabaseHelper databaseHelper;
    private SimpleDateFormat dateFormat, timeFormat;

    private TextView tvGreeting, tvPendingTasks, tvTotalCompleted, tvProgressPercentage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Initialize Database and Formats --- //
        databaseHelper = new DatabaseHelper(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

        // --- Views --- //
        tvGreeting = findViewById(R.id.tvGreeting);
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        tvTotalCompleted = findViewById(R.id.tvTotalCompleted);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        progressBar = findViewById(R.id.progress_bar);
        TextView tvSeeAll = findViewById(R.id.tvSeeAll);
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);
        RecyclerView recyclerTaskGroups = findViewById(R.id.recyclerTaskGroups);

        // --- Setup Listeners --- //
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TarefasActivity.class);
            startActivity(intent);
        });

        btnAddTask.setOnClickListener(v -> showAddTaskDialog());

        // --- Setup Task Groups RecyclerView --- //
        recyclerTaskGroups.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        String today = dateFormat.format(Calendar.getInstance().getTime());

        // --- Update Greeting --- //
        Calendar c = Calendar.getInstance();
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hourOfDay >= 0 && hourOfDay < 12) {
            greeting = "Bom dia!";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "Boa tarde!";
        } else {
            greeting = "Boa noite!";
        }
        tvGreeting.setText(greeting);

        // --- Update Header --- //
        int pendingCount = databaseHelper.getTasksCount(today, false);
        tvPendingTasks.setText(String.format(Locale.getDefault(), "%d atividades pendentes", pendingCount));

        // --- Update Task Groups --- //
        List<Task> tasksFromDb = databaseHelper.getTasksByDate(today);
        this.taskGroups = tasksFromDb.stream()
                .map(task -> new TaskGroup(task.getId(), task.getDescription(), task.getTime(), "1 tarefa", task.isCompleted()))
                .collect(Collectors.toList());
        adapter = new TaskGroupAdapter(taskGroups, this);
        ((RecyclerView) findViewById(R.id.recyclerTaskGroups)).setAdapter(adapter);

        // --- Update Progress Card --- //
        int totalToday = databaseHelper.getTasksCount(today, null);
        int completedToday = databaseHelper.getTasksCount(today, true);
        int totalCompleted = databaseHelper.getTotalCompletedTasks();

        int progressPercentage = 0;
        if (totalToday > 0) {
            progressPercentage = (int) (((double) completedToday / totalToday) * 100);
        }

        progressBar.setProgress(progressPercentage);
        tvProgressPercentage.setText(String.format(Locale.getDefault(), "%d%%", progressPercentage));
        tvTotalCompleted.setText(String.format(Locale.getDefault(), "%d+ atividades feitas", totalCompleted));
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
                    MainActivity.this,
                    (view, hourOfDay, minute) -> {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute < 30 ? 0 : 30);
                        String timeStr = timeFormat.format(c.getTime());
                        etTaskTime.setText(timeStr);
                    },
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
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
            databaseHelper.addTask(newTask);
            updateUI(); // Refresh the Home screen
            Toast.makeText(this, "Tarefa adicionada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onGroupClick(int position) {
        // 1. Get the clicked group and its original ID
        TaskGroup clickedGroup = taskGroups.get(position);
        int taskId = clickedGroup.getId();
        boolean newStatus = !clickedGroup.isCompleted();

        // 2. Update the database
        databaseHelper.updateTaskCompletionStatus(taskId, newStatus);

        // 3. Update the local list and notify the adapter
        clickedGroup.setCompleted(newStatus);
        adapter.notifyItemChanged(position);

        // 4. Also, refresh the progress card
        updateUI();
    }
}
