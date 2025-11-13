package com.example.tarefas;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class TaskGroupAdaptativo extends RecyclerView.Adapter<TaskGroupAdaptativo.TaskGrupoViewHolder> {

    private List<TaskGrupo> taskGroups;
    private OnGroupClickListener listener;

   
    public interface OnGroupClickListener {
        void onGroupClick(int position);
    }

    /**
      Construtor do adaptador.
     * @param taskGroups lista de grupos de tarefas
     * @param listener listener para capturar o clique em um grupo
     */
    public TaskGroupAdaptativo(List<TaskGrupo> taskGroups, OnGroupClickListener listener) {
        this.taskGroups = taskGroups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskGrupoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_group, parent, false);
        return new TaskGrupoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskGrupoViewHolder holder, int position) {
        TaskGrupo taskGroup = taskGroups.get(position);
        holder.tvGroupTitle.setText(taskGroup.getTitle());
        holder.tvGroupTime.setText(taskGroup.getTime());
        holder.tvGroupTaskCount.setText(taskGroup.getTaskCount());

        // aplica ou remove o risco do texto com base no estado de conclusão do grupo
        if (taskGroup.isCompleted()) {
            holder.tvGroupTitle.setPaintFlags(holder.tvGroupTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvGroupTitle.setPaintFlags(holder.tvGroupTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        
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


    public static class TaskGrupoViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupTitle, tvGroupTime, tvGroupTaskCount;

        public TaskGrupoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupTitle = itemView.findViewById(R.id.tvGroupTitle);
            tvGroupTime = itemView.findViewById(R.id.tvGroupTime);
            tvGroupTaskCount = itemView.findViewById(R.id.tvGroupTaskCount);
        }
    }
}
