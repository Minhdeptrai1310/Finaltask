package com.example.finaltask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finaltask.notification.AlarmReceiver;
import com.example.finaltask.notification.NotificationHelper;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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

        new NotificationHelper(this);
        // Kiểm tra và yêu cầu quyền thông báo trên Android 13+
        checkNotificationPermission();

        // Khởi tạo view và cấu hình RecyclerView
        initializeViews();
        setupRecyclerView();

        // Cập nhật ngày hiện tại và xử lý sự kiện
        updateCurrentDate();
        setupEventListeners();
    }

    /**
     * Kiểm tra và yêu cầu quyền hiển thị thông báo trên Android 13+
     */
    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1
                );
            }
        }
    }

    /**
     * Ánh xạ view và khởi tạo FirebaseFirestore
     */
    private void initializeViews() {
        recyclerView = findViewById(R.id.rvTask);
        tvDate = findViewById(R.id.tvDate);
        fabAddTask = findViewById(R.id.FloatingActionButton);
        btnAccount = findViewById(R.id.btnAccount);
        db = FirebaseFirestore.getInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Cập nhật ngày hiện tại lên giao diện
     */
    private void updateCurrentDate() {
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);
    }

    /**
     * Thiết lập FirestoreRecyclerAdapter cho RecyclerView
     */
    private void setupRecyclerView() {
        Query query = db.collection("tasks").orderBy("taskDateTime", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();
        taskAdapter = new TaskAdapter(options);
        recyclerView.setAdapter(taskAdapter);
    }

    /**
     * Xử lý sự kiện cho các nút và swipe
     */
    private void setupEventListeners() {
        fabAddTask.setOnClickListener(v ->
                startActivityForResult(new Intent(this, AddNewTask.class), 1)
        );
        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class))
        );
        setupSwipeToDelete();
        setupCategoryButtons();
    }

    /**
     * Thiết lập chức năng swipe để xóa task
     */
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
                int position = viewHolder.getAdapterPosition();
                DocumentSnapshot snapshot = taskAdapter.getSnapshots().getSnapshot(position);
                handleTaskDeletion(position, snapshot);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
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

    /**
     * Xử lý việc xóa task khi người dùng swipe
     */
    private void handleTaskDeletion(int position, DocumentSnapshot snapshot) {
        String documentId = snapshot.getId();
        String taskName = snapshot.getString("taskName");
        showDeleteConfirmationDialog(position, documentId, taskName);
    }

    /**
     * Hiển thị hộp thoại xác nhận xóa task
     */
    private void showDeleteConfirmationDialog(int position, String documentId, String taskName) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa công việc: " + taskName + "?")
                .setPositiveButton("Có", (dialog, which) -> deleteTask(position, documentId))
                .setNegativeButton("Không", (dialog, which) -> taskAdapter.notifyItemChanged(position))
                .show();
    }

    /**
     * Xóa task từ Firestore và hủy thông báo liên quan
     */
    private void deleteTask(int position, String documentId) {
        cancelAlarm(documentId);
        db.collection("tasks").document(documentId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                    updateCategoryCounts();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi xóa công việc", Toast.LENGTH_SHORT).show();
                    taskAdapter.notifyItemChanged(position);
                });
    }

    /**
     * Hủy thông báo dựa trên documentId
     */
    private void cancelAlarm(String documentId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        int requestCode = documentId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d("MainActivity", "Đã hủy thông báo cho task: " + documentId);
        }
    }

    /**
     * Cập nhật số lượng task theo từng danh mục
     */
    private void updateCategoryCounts() {
        Map<String, Integer> counts = new HashMap<>();
        String[] categories = {"Sức khỏe", "Công việc", "Sức khỏe tinh thần", "Khác"};
        for (String category : categories) counts.put(category, 0);

        db.collection("tasks").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                String category = snapshot.getString("taskCategory");
                counts.put(category, counts.getOrDefault(category, 0) + 1);
            }
            updateCategoryButtons(counts);
        });
    }

    /**
     * Cập nhật giao diện các nút danh mục
     */
    private void updateCategoryButtons(Map<String, Integer> counts) {
        ((Button) findViewById(R.id.btnHealth)).setText("Sức khỏe: " + counts.get("Sức khỏe"));
        ((Button) findViewById(R.id.btnWork)).setText("Công việc: " + counts.get("Công việc"));
        ((Button) findViewById(R.id.btnMental)).setText("Sức khỏe tinh thần: " + counts.get("Sức khỏe tinh thần"));
        ((Button) findViewById(R.id.btnOthers)).setText("Khác: " + counts.get("Khác"));
    }

    /**
     * Thiết lập sự kiện cho các nút danh mục
     */
    private void setupCategoryButtons() {
        findViewById(R.id.btnHealth).setOnClickListener(v -> toggleCategory("Sức khỏe"));
        findViewById(R.id.btnWork).setOnClickListener(v -> toggleCategory("Công việc"));
        findViewById(R.id.btnMental).setOnClickListener(v -> toggleCategory("Sức khỏe tinh thần"));
        findViewById(R.id.btnOthers).setOnClickListener(v -> toggleCategory("Khác"));
    }

    /**
     * Chuyển đổi giữa các chế độ lọc danh mục
     */
    private void toggleCategory(String category) {
        if (category.equals(selectedCategory)) {
            selectedCategory = null;
            showAllTasks();
        } else {
            selectedCategory = category;
            filterTasksByCategory(category);
        }
        updateButtonStates(category);
    }

    /**
     * Cập nhật trạng thái selected cho các nút danh mục
     */
    private void updateButtonStates(String category) {
        int[] buttonIds = {R.id.btnHealth, R.id.btnWork, R.id.btnMental, R.id.btnOthers};
        for (int id : buttonIds) findViewById(id).setSelected(false);
        switch (category) {
            case "Sức khỏe": findViewById(R.id.btnHealth).setSelected(true); break;
            case "Công việc": findViewById(R.id.btnWork).setSelected(true); break;
            case "Sức khỏe tinh thần": findViewById(R.id.btnMental).setSelected(true); break;
            case "Khác": findViewById(R.id.btnOthers).setSelected(true); break;
        }
    }

    /**
     * Hiển thị tất cả tasks không lọc
     */
    private void showAllTasks() {
        taskAdapter.updateOptions(new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(db.collection("tasks").orderBy("taskDateTime"), Task.class)
                .build());
    }

    /**
     * Lọc tasks theo danh mục
     */
    private void filterTasksByCategory(String category) {
        taskAdapter.updateOptions(new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(db.collection("tasks")
                        .whereEqualTo("taskCategory", category)
                        .orderBy("taskDateTime"), Task.class)
                .build());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Task newTask = (Task) data.getSerializableExtra("new_task");
            if (newTask != null) {
                db.collection("tasks").add(newTask)
                        .addOnSuccessListener(documentReference -> updateCategoryCounts())
                        .addOnFailureListener(e -> Log.e("MainActivity", "Lỗi thêm task", e));
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
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Đã cấp quyền thông báo");
            } else {
                Log.d("MainActivity", "Từ chối quyền thông báo");
            }
        }
    }
}