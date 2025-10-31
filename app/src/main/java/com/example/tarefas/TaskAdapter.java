package com.example.tarefas;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onCheckClick(Task task, int position);
        void onDeleteClick(Task task, int position);
    }

    public TaskAdapter(List<Task> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void setTasks(List<Task> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTime.setText(task.getTime());

        // A 'real' task is one that has a description. Placeholders do not.
        boolean isRealTask = task.getDescription() != null && !task.getDescription().isEmpty();

        if (isRealTask) {
            // --- CONFIGURE FOR A REAL TASK ---
            holder.cardContainer.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(task.getDescription());

            // Handle the completed state (strikethrough text)
            if (task.isCompleted()) {
                holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvDescription.setPaintFlags(holder.tvDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Set listeners for the buttons
            holder.btnCheck.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCheckClick(task, position);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(task, position);
                }
            });

        } else {
            // --- CONFIGURE FOR AN EMPTY SLOT ---
            // Hide the card
            holder.cardContainer.setVisibility(View.INVISIBLE);
            
            // Clean up previous state to prevent bugs from view recycling
            holder.tvDescription.setText("");
            holder.tvDescription.setPaintFlags(0);
            holder.btnCheck.setOnClickListener(null);
            holder.btnDelete.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvDescription;
        ImageView btnCheck, btnDelete;
        RelativeLayout cardContainer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTaskTime);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            btnCheck = itemView.findViewById(R.id.btnCheck);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            cardContainer = itemView.findViewById(R.id.card_container);
        }
    }
}