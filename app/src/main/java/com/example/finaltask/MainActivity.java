package com.example.finaltask;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private FirebaseFirestore db;
    private TextView tvDate;
    private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ view
        recyclerView = findViewById(R.id.rvTask);
        tvDate = findViewById(R.id.tvDate);
        fabAddTask = findViewById(R.id.FloatingActionButton);

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Cập nhật ngày tháng
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);

        // Thiết lập Firestore Recycler Adapter
        setupRecyclerView();

        // Thiết lập swipe để xóa
        setupSwipeToDelete();

        // Xử lý thêm task mới
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNewTask.class);
            startActivityForResult(intent, 1);
        });
    }

    private void setupRecyclerView() {
        Query query = db.collection("tasks")
                .orderBy("taskDateTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        taskAdapter = new TaskAdapter(options);
        recyclerView.setAdapter(taskAdapter);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Lấy document ID từ vị trí swipe
                DocumentSnapshot snapshot = taskAdapter.getSnapshots()
                        .getSnapshot(viewHolder.getAdapterPosition());
                db.collection("tasks").document(snapshot.getId()).delete()
                        .addOnSuccessListener(aVoid -> Log.d("MainActivity", "Task deleted"))
                        .addOnFailureListener(e -> Log.w("MainActivity", "Error deleting task", e));
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Task newTask = (Task) data.getSerializableExtra("new_task");
            if (newTask != null) {
                db.collection("tasks").add(newTask)
                        .addOnSuccessListener(documentReference ->
                                Log.d("MainActivity", "Document added with ID: " + documentReference.getId()))
                        .addOnFailureListener(e ->
                                Log.w("MainActivity", "Error adding document", e));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        taskAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        taskAdapter.stopListening();
    }
}