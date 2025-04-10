package com.example.finaltask;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.view.View;

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
        setupLongClickToShare();

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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Query query = db.collection("tasks")
                .whereArrayContainsAny("sharedWithOrOwner", java.util.Arrays.asList(uid, email))
                .orderBy("taskDateTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        taskAdapter = new TaskAdapter(options);
        recyclerView.setAdapter(taskAdapter);
    }

    private void updateCategoryCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Sức khỏe", 0);
        counts.put("Công việc", 0);
        counts.put("Sức khỏe tinh thần", 0);
        counts.put("Khác", 0);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("tasks")
                .whereArrayContainsAny("sharedWithOrOwner", java.util.Arrays.asList(uid, email))
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (DocumentSnapshot doc : snapshots) {
                        String cat = doc.getString("taskCategory");
                        if (counts.containsKey(cat)) {
                            counts.put(cat, counts.get(cat) + 1);
                        }
                    }
                    updateCategoryButtons(counts);
                });
    }

    private void updateCategoryButtons(Map<String, Integer> counts) {
        ((Button) findViewById(R.id.btnHealth)).setText("Sức khỏe: " + counts.get("Sức khỏe"));
        ((Button) findViewById(R.id.btnWork)).setText("Công việc: " + counts.get("Công việc"));
        ((Button) findViewById(R.id.btnMental)).setText("Sức khỏe tinh thần: " + counts.get("Sức khỏe tinh thần"));
        ((Button) findViewById(R.id.btnOthers)).setText("Khác: " + counts.get("Khác"));
    }

    private void setupCategoryButtons() {
        findViewById(R.id.btnHealth).setOnClickListener(v -> toggleCategory("Sức khỏe"));
        findViewById(R.id.btnWork).setOnClickListener(v -> toggleCategory("Công việc"));
        findViewById(R.id.btnMental).setOnClickListener(v -> toggleCategory("Sức khỏe tinh thần"));
        findViewById(R.id.btnOthers).setOnClickListener(v -> toggleCategory("Khác"));
    }

    private void toggleCategory(String category) {
        if (category.equals(selectedCategory)) {
            selectedCategory = null;
            resetButtonStates();
            setupRecyclerView();
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
            case "Sức khỏe": findViewById(R.id.btnHealth).setSelected(true); break;
            case "Công việc": findViewById(R.id.btnWork).setSelected(true); break;
            case "Sức khỏe tinh thần": findViewById(R.id.btnMental).setSelected(true); break;
            case "Khác": findViewById(R.id.btnOthers).setSelected(true); break;
        }
    }

    private void filterTasksByCategory(String category) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Query query = db.collection("tasks")
                .whereEqualTo("taskCategory", category)
                .whereArrayContainsAny("sharedWithOrOwner", java.util.Arrays.asList(uid, email))
                .orderBy("taskDateTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        taskAdapter.updateOptions(options);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v1, @NonNull RecyclerView.ViewHolder v2) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                DocumentSnapshot snapshot = taskAdapter.getSnapshots().getSnapshot(pos);
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
                                        Toast.makeText(MainActivity.this, "Lỗi xóa", Toast.LENGTH_SHORT).show();
                                        taskAdapter.notifyItemChanged(pos);
                                    });
                        })
                        .setNegativeButton("Không", (dialog, which) -> taskAdapter.notifyItemChanged(pos))
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean isActive) {
                new RecyclerViewSwipeDecorator.Builder(c, r, vh, dX, dY, actionState, isActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light))
                        .addSwipeLeftActionIcon(R.drawable.ic_delete)
                        .addSwipeLeftLabel("XÓA")
                        .setSwipeLeftLabelColor(ContextCompat.getColor(MainActivity.this, android.R.color.white))
                        .create().decorate();
                super.onChildDraw(c, r, vh, dX, dY, actionState, isActive);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void setupLongClickToShare() {
        taskAdapter.setOnItemLongClickListener((documentSnapshot, position) -> {
            showShareDialog(documentSnapshot.getId());
        });
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
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Đã chia sẻ!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Lỗi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Task newTask = (Task) data.getSerializableExtra("new_task");
            if (newTask != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                newTask.setUserId(uid);
                List<String> sharedWithOrOwner = new ArrayList<>();
                sharedWithOrOwner.add(uid);
                sharedWithOrOwner.add(email);
                newTask.setSharedWithOrOwner(sharedWithOrOwner);

                db.collection("tasks").add(newTask)
                        .addOnSuccessListener(ref -> {
                            Log.d("MainActivity", "Đã thêm task ID: " + ref.getId());
                            updateCategoryCounts();
                        })
                        .addOnFailureListener(e -> Log.e("MainActivity", "Lỗi thêm task", e));
            }
        }
    }

    @Override protected void onStart() {
        super.onStart();
        if (taskAdapter != null) taskAdapter.startListening();
        updateCategoryCounts();
    }

    @Override protected void onStop() {
        super.onStop();
        if (taskAdapter != null) taskAdapter.stopListening();
    }
}
