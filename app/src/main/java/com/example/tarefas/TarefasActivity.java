package com.example.tarefas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TarefasActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerViewTasks;
    private FloatingActionButton btnAddTask;
    private TextView tvDate, tvTaskCount;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private DatabaseHelper databaseHelper;
    private Calendar calendar;
    private SimpleDateFormat dateFormat, monthFormat, timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        btnAddTask = findViewById(R.id.btnAddTask);
        tvDate = findViewById(R.id.tvDate);
        tvTaskCount = findViewById(R.id.tvTaskCount);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnSettings = findViewById(R.id.btnSettings);

        databaseHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("pt", "BR"));
        monthFormat = new SimpleDateFormat("MMMM dd", new Locale("pt", "BR"));
        timeFormat = new SimpleDateFormat("h:mm a", Locale.US);

        updateDateDisplay();
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        loadTasksAndScroll();

        btnAddTask.setOnClickListener(v -> showAddTaskDialog());
        btnBack.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v -> Toast.makeText(this, "Configurações", Toast.LENGTH_SHORT).show());
    }

    private void updateDateDisplay() {
        String displayDate = monthFormat.format(calendar.getTime());
        displayDate = displayDate.substring(0, 1).toUpperCase() + displayDate.substring(1);
        tvDate.setText(displayDate);
    }

    private void loadTasksAndScroll() {
        String currentDate = dateFormat.format(calendar.getTime());
        Map<String, List<Task>> tasksByTime = databaseHelper.getTasksByDate(currentDate).stream()
                .collect(Collectors.groupingBy(Task::getTime));

        List<Task> fullDayTaskList = new ArrayList<>();
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        tempCal.set(Calendar.MINUTE, 0);

        for (int i = 0; i < 48; i++) {
            String timeSlot = timeFormat.format(tempCal.getTime());
            List<Task> tasksForSlot = tasksByTime.get(timeSlot);

            if (tasksForSlot != null && !tasksForSlot.isEmpty()) {
                fullDayTaskList.addAll(tasksForSlot);
            } else {
                // Add a placeholder for empty slots to maintain the timeline
                fullDayTaskList.add(new Task(-1, currentDate, timeSlot, null));
            }
            tempCal.add(Calendar.MINUTE, 30);
        }
        this.taskList = fullDayTaskList;

        if (taskAdapter == null) {
            taskAdapter = new TaskAdapter(this.taskList, this);
            recyclerViewTasks.setAdapter(taskAdapter);
        } else {
            taskAdapter.setTasks(this.taskList);
        }

        long actualTaskCount = tasksByTime.values().stream().mapToLong(List::size).sum();
        String taskCountText = String.format(Locale.getDefault(), "%02d tarefas hoje", actualTaskCount);
        tvTaskCount.setText(taskCountText);

        scrollToCurrentTime();
    }

    private void scrollToCurrentTime() {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int position = currentHour * 2 + (currentMinute >= 30 ? 1 : 0);

        if (position > 0 && position < taskList.size()) {
            ((LinearLayoutManager) recyclerViewTasks.getLayoutManager()).scrollToPositionWithOffset(position, 0);
        }
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
        etTaskDate.setText(dateFormat.format(calendar.getTime()));

        etTaskTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    TarefasActivity.this,
                    (view, hourOfDay, minute) -> {
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute < 30 ? 0 : 30); // RE-ADDED ROUNDING TO FIX BUG
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
            loadTasksAndScroll();
            Toast.makeText(this, "Tarefa adicionada", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onCheckClick(Task task, int position) {
        task.setCompleted(!task.isCompleted());
        databaseHelper.updateTaskCompletionStatus(task.getId(), task.isCompleted());
        loadTasksAndScroll();
    }

    @Override
    public void onDeleteClick(Task task, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar Tarefa")
                .setMessage("Tem certeza que deseja deletar esta tarefa?")
                .setPositiveButton("Deletar", (dialog, which) -> {
                    databaseHelper.deleteTask(task.getId());
                    loadTasksAndScroll();
                    Toast.makeText(TarefasActivity.this, "Tarefa deletada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}