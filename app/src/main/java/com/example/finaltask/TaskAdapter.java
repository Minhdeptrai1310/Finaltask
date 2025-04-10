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

    public interface OnItemLongClickListener {
        void onItemLongClick(DocumentSnapshot snapshot, int position);
    }

    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public TaskAdapter(@NonNull FirestoreRecyclerOptions<Task> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull Task model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.each_task, parent, false);
        return new TaskViewHolder(v);
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate;

        public TaskViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.taskName);
            tvDate = itemView.findViewById(R.id.taskDateTime);

            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                }
                return true;
            });
        }

        public void bind(Task task) {
            tvName.setText(task.getTaskName());
            tvDate.setText(task.getTaskDateTime());
        }
    }
}
