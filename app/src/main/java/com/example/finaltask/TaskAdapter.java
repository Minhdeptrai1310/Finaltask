package com.example.finaltask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.firestore.DocumentSnapshot;

public class TaskAdapter extends FirestoreRecyclerAdapter<Task, TaskAdapter.TaskViewHolder> {

    // Interface callback cho sự kiện long-click (chia sẻ)
    public interface OnItemLongClickListener {
        void onItemLongClick(@NonNull DocumentSnapshot snapshot);
    }

    private OnItemLongClickListener longClickListener;

    // Setter để thiết lập listener
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public TaskAdapter(@NonNull FirestoreRecyclerOptions<Task> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull Task task) {
        // Bind Task object to ViewHolder
        holder.taskName.setText(task.getTaskName());
        holder.taskDateTime.setText(task.getTaskDateTime());
        holder.taskCategory.setText(task.getTaskCategory());

        // Xử lý trạng thái checkbox nếu cần
        holder.taskName.setChecked(false); // Cập nhật logic theo nhu cầu

        // Thêm sự kiện long-click cho itemView để chia sẻ công việc
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                // Lấy DocumentSnapshot của item tại vị trí hiện tại
                DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                longClickListener.onItemLongClick(snapshot);
                return true;
            }
            return false;
        });
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.each_task, parent, false);
        return new TaskViewHolder(view);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox taskName;
        TextView taskDateTime;
        TextView taskCategory;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskDateTime = itemView.findViewById(R.id.taskDateTime);
            taskCategory = itemView.findViewById(R.id.taskCategory);

            taskName.setText(itemView.getContext().getString(R.string.task_name_default));
        }
    }

    public void updateOptions(FirestoreRecyclerOptions<Task> newOptions) {
        super.updateOptions(newOptions);
    }
}
