package com.example.finaltask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getTaskName());
        holder.taskDateTime.setText(task.getTaskDateTime());
        holder.taskCategory.setText(task.getTaskCategory());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Phương thức cập nhật danh sách công việc
    public void updateTasks(List<Task> newTasks) {
        taskList.clear();
        taskList.addAll(newTasks);
        notifyDataSetChanged(); // Cập nhật giao diện
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskDateTime, taskCategory;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskDateTime = itemView.findViewById(R.id.taskDateTime);
            taskCategory = itemView.findViewById(R.id.taskCategory);
        }
    }
}
