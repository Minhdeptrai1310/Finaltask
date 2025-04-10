package com.example.finaltask;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private FirebaseFirestore db;
    private TextView tvDate;
    private FloatingActionButton fabAddTask;
    private Button btnAccount;
    private String selectedCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        recyclerView = findViewById(R.id.rvTask);
        tvDate = findViewById(R.id.tvDate);
        fabAddTask = findViewById(R.id.FloatingActionButton);
        btnAccount = findViewById(R.id.btnAccount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);

        setupRecyclerView();
        setupSwipeToDelete();

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNewTask.class);
            startActivityForResult(intent, 1);
        });

        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
            startActivity(intent);
        });

        setupCategoryButtons();
    }

    private void setupRecyclerView() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Query query = db.collection("tasks")
                .whereArrayContainsAny("sharedWithOrOwner", java.util.Arrays.asList(currentUserId, currentUserEmail))
                .orderBy("taskDateTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        taskAdapter = new TaskAdapter(options);
        recyclerView.setAdapter(taskAdapter);
    }

    private void updateCategoryCounts() {
        Map<String, Integer> categoryCounts = new HashMap<>();
        categoryCounts.put("Sức khỏe", 0);
        categoryCounts.put("Công việc", 0);
        categoryCounts.put("Sức khỏe tinh thần", 0);
        categoryCounts.put("Khác", 0);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("tasks")
                .whereArrayContainsAny("sharedWithOrOwner", java.util.Arrays.asList(currentUserId, currentUserEmail))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String category = snapshot.getString("taskCategory");
                        if (categoryCounts.containsKey(category)) {
                            categoryCounts.put(category, categoryCounts.get(category) + 1);
                        }
                    }
                    updateCategoryButtons(categoryCounts);
                });
    }

    private void updateCategoryButtons(Map<String, Integer> counts) {
        Button btnHealth = findViewById(R.id.btnHealth);
        btnHealth.setText("Sức khỏe: " + counts.get("Sức khỏe"));

        Button btnWork = findViewById(R.id.btnWork);
        btnWork.setText("Công việc: " + counts.get("Công việc"));

        Button btnMental = findViewById(R.id.btnMental);
        btnMental.setText("Sức khỏe tinh thần: " + counts.get("Sức khỏe tinh thần"));

        Button btnOthers = findViewById(R.id.btnOthers);
        btnOthers.setText("Khác: " + counts.get("Khác"));
    }

    private void setupCategoryButtons() {
        Button btnHealth = findViewById(R.id.btnHealth);
        Button btnWork = findViewById(R.id.btnWork);
        Button btnMental = findViewById(R.id.btnMental);
        Button btnOthers = findViewById(R.id.btnOthers);

        btnHealth.setOnClickListener(v -> toggleCategory("Sức khỏe"));
        btnWork.setOnClickListener(v -> toggleCategory("Công việc"));
        btnMental.setOnClickListener(v -> toggleCategory("Sức khỏe tinh thần"));
        btnOthers.setOnClickListener(v -> toggleCategory("Khác"));
    }

    private void toggleCategory(String category) {
        if (category.equals(selectedCategory)) {
            selectedCategory = null;
            resetButtonStates();
            showAllTasks();
        } else {
            selectedCategory = category;
            resetButtonStates();
            setButtonSelected(category);
            filterTasksByCategory(category);
        }
    }

    private void resetButtonStates() {
        findViewById(R.id.btnHealth).setSelected(false);
        findViewById(R.id.btnWork).setSelected(false);
        findViewById(R.id.btnMental).setSelected(false);
        findViewById(R.id.btnOthers).setSelected(false);
    }

    private void setButtonSelected(String category) {
        switch (category) {
            case "Sức khỏe":
                findViewById(R.id.btnHealth).setSelected(true);
                break;
            case "Công việc":
                findViewById(R.id.btnWork).setSelected(true);
                break;
            case "Sức khỏe tinh thần":
                findViewById(R.id.btnMental).setSelected(true);
                break;
            case "Khác":
                findViewById(R.id.btnOthers).setSelected(true);
                break;
        }
    }

    private void showAllTasks() {
        setupRecyclerView();
    }

    private void filterTasksByCategory(String category) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Query query = db.collection("tasks")
                .whereEqualTo("taskCategory", category)
                .whereArrayContainsAny("sharedWithOrOwner", java.util.Arrays.asList(currentUserId, currentUserEmail))
                .orderBy("taskDateTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        taskAdapter.updateOptions(options);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                DocumentSnapshot snapshot = taskAdapter.getSnapshots().getSnapshot(position);
                String taskName = snapshot.getString("taskName");

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa công việc: " + taskName + "?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            db.collection("tasks").document(snapshot.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                                        updateCategoryCounts();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Lỗi khi xóa công việc", Toast.LENGTH_SHORT).show();
                                        taskAdapter.notifyItemChanged(position);
                                    });
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            taskAdapter.notifyItemChanged(position);
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("XÓA")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(MainActivity.this, android.R.color.white))
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void showShareDialog(String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Chia sẻ công việc");

        final EditText input = new EditText(MainActivity.this);
        input.setHint("Nhập email người nhận");
        builder.setView(input);

        builder.setPositiveButton("Chia sẻ", (dialog, which) -> {
            String shareEmail = input.getText().toString().trim();
            if (!shareEmail.isEmpty()) {
                shareTask(documentId, shareEmail);
            } else {
                Toast.makeText(MainActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void shareTask(String documentId, String shareEmail) {
        db.collection("tasks").document(documentId)
                .update("sharedWith", com.google.firebase.firestore.FieldValue.arrayUnion(shareEmail),
                        "sharedWithOrOwner", com.google.firebase.firestore.FieldValue.arrayUnion(shareEmail))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Công việc đã được chia sẻ!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Lỗi chia sẻ công việc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Task newTask = (Task) data.getSerializableExtra("new_task");
            if (newTask != null) {
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                newTask.setUserId(currentUserId);

                List<String> sharedWithOrOwner = new ArrayList<>();
                sharedWithOrOwner.add(currentUserId);
                sharedWithOrOwner.add(currentUserEmail);
                newTask.setSharedWithOrOwner(sharedWithOrOwner);

                db.collection("tasks").add(newTask)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("MainActivity", "Document added with ID: " + documentReference.getId());
                            updateCategoryCounts();
                        })
                        .addOnFailureListener(e -> Log.w("MainActivity", "Error adding document", e));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        taskAdapter.startListening();
        updateCategoryCounts();
    }

    @Override
    protected void onStop() {
        super.onStop();
        taskAdapter.stopListening();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Permission granted");
            } else {
                Log.d("MainActivity", "Permission denied");
            }
        }
    }
}
