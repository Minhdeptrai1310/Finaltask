package com.example.finaltask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskAdapter extends FirestoreRecyclerAdapter<Task, TaskAdapter.TaskViewHolder> {

    private OnShareClickListener onShareClickListener;

    public interface OnShareClickListener {
        void onShareClick(DocumentSnapshot documentSnapshot);
    }

    public void setOnShareClickListener(OnShareClickListener listener) {
        this.onShareClickListener = listener;
    }

    public TaskAdapter(@NonNull FirestoreRecyclerOptions<Task> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull Task model) {
        holder.checkBox.setText(model.getTaskName());
        holder.tvDateTime.setText(model.getTaskDateTime());
        holder.tvCategory.setText(model.getTaskCategory());

        holder.ivShare.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && onShareClickListener != null) {
                DocumentSnapshot snapshot = getSnapshots().getSnapshot(pos);
                onShareClickListener.onShareClick(snapshot);
            }
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
        MaterialCheckBox checkBox;
        TextView tvDateTime, tvCategory;
        ImageView ivShare;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.taskName);
            tvDateTime = itemView.findViewById(R.id.taskDateTime);
            tvCategory = itemView.findViewById(R.id.taskCategory);
            ivShare = itemView.findViewById(R.id.ivShare); // Kết nối ImageView chia sẻ
        }
    }
}
