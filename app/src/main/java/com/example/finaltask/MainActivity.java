package com.example.finaltask;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kiểm tra quyền POST_NOTIFICATIONS trên Android 13 trở lên
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);  // 1 là mã yêu cầu
            }
        }

        // Ánh xạ view
        recyclerView = findViewById(R.id.rvTask);
        tvDate = findViewById(R.id.tvDate);
        fabAddTask = findViewById(R.id.FloatingActionButton);
        btnAccount = findViewById(R.id.btnAccount);  // Ánh xạ nút btnAccount

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

        // Xử lý sự kiện cho nút btnAccount
        btnAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccountActivity.class); // Mở AccountActivity
            startActivity(intent);  // Bắt đầu Activity mới
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

    private void updateCategoryCounts() {
        // Khởi tạo biến đếm
        Map<String, Integer> categoryCounts = new HashMap<>();
        categoryCounts.put("Sức khỏe", 0);
        categoryCounts.put("Công việc", 0);
        categoryCounts.put("Sức khỏe tinh thần", 0);
        categoryCounts.put("Khác", 0);

        // Lấy dữ liệu từ Firestore
        db.collection("tasks")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String category = snapshot.getString("taskCategory");
                        if (categoryCounts.containsKey(category)) {
                            categoryCounts.put(category, categoryCounts.get(category) + 1);
                        }
                    }
                    // Cập nhật UI
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

    // Trong MainActivity.java
    private void setupCategoryButtons() {
        Button btnHealth = findViewById(R.id.btnHealth);
        btnHealth.setOnClickListener(v -> filterTasksByCategory("Sức khỏe"));

        Button btnWork = findViewById(R.id.btnWork);
        btnWork.setOnClickListener(v -> filterTasksByCategory("Công việc"));

        Button btnMental = findViewById(R.id.btnMental);
        btnMental.setOnClickListener(v -> filterTasksByCategory("Sức khỏe tinh thần"));

        Button btnOthers = findViewById(R.id.btnOthers);
        btnOthers.setOnClickListener(v -> filterTasksByCategory("Khác"));
    }

    private void filterTasksByCategory(String category) {
        Query query = db.collection("tasks")
                .whereEqualTo("taskCategory", category)
                .orderBy("taskDateTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        taskAdapter.updateOptions(options); // Cập nhật Adapter với query mới
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
                int position = viewHolder.getAdapterPosition();
                DocumentSnapshot snapshot = taskAdapter.getSnapshots().getSnapshot(position);
                String taskName = snapshot.getString("taskName");

                // Hiển thị dialog xác nhận
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa công việc: " + taskName + "?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Xóa task nếu người dùng đồng ý
                            db.collection("tasks").document(snapshot.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Lỗi khi xóa công việc", Toast.LENGTH_SHORT).show();
                                        taskAdapter.notifyItemChanged(position); // Khôi phục item nếu có lỗi
                                    });
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            // Khôi phục item nếu người dùng không đồng ý
                            taskAdapter.notifyItemChanged(position);
                        })
                        .setCancelable(false)
                        .show();
            }

            // Thêm màu nền và icon khi swipe
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
        updateCategoryCounts();
    }

    @Override
    protected void onStop() {
        super.onStop();
        taskAdapter.stopListening();
    }

    // Xử lý kết quả yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, bạn có thể gửi thông báo
                Log.d("MainActivity", "Permission granted");
            } else {
                // Quyền bị từ chối, bạn có thể thông báo cho người dùng
                Log.d("MainActivity", "Permission denied");
            }
        }
    }
}
