package com.example.tarefas;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskGroupViewHolder> {

    private List<TaskGroup> taskGroups;
    private OnGroupClickListener listener;

    // Interface para comunicar o clique para a MainActivity
    public interface OnGroupClickListener {
        void onGroupClick(int position);
    }

    public TaskGroupAdapter(List<TaskGroup> taskGroups, OnGroupClickListener listener) {
        this.taskGroups = taskGroups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_group, parent, false);
        return new TaskGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskGroupViewHolder holder, int position) {
        TaskGroup taskGroup = taskGroups.get(position);
        holder.tvGroupTitle.setText(taskGroup.getTitle());
        holder.tvGroupTime.setText(taskGroup.getTime());
        holder.tvGroupTaskCount.setText(taskGroup.getTaskCount());

        // Aplica ou remove o risco do texto baseado no estado da tarefa
        if (taskGroup.isCompleted()) {
            holder.tvGroupTitle.setPaintFlags(holder.tvGroupTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvGroupTitle.setPaintFlags(holder.tvGroupTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // Define o listener para o clique no card inteiro
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGroupClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskGroups.size();
    }

    public static class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupTitle, tvGroupTime, tvGroupTaskCount;

        public TaskGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupTitle = itemView.findViewById(R.id.tvGroupTitle);
            tvGroupTime = itemView.findViewById(R.id.tvGroupTime);
            tvGroupTaskCount = itemView.findViewById(R.id.tvGroupTaskCount);
        }
    }
}
