package com.example.finaltask;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private FirebaseDatabase database;
    private DatabaseReference taskRef;
    private TextView tvDate;
    private FloatingActionButton fabAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rvTask);
        tvDate = findViewById(R.id.tvDate);
        fabAddTask = findViewById(R.id.FloatingActionButton);  // Đổi ID chính xác
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Cập nhật ngày tháng động
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance();
        taskRef = database.getReference("tasks");  // Tham chiếu đến "tasks" trong Firebase

        // Tạo danh sách công việc và Adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(taskAdapter);

        // Lấy dữ liệu từ Firebase
        loadTasksFromFirebase();

        // Xử lý sự kiện khi nhấn nút "+"
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddNewTask.class);
                startActivityForResult(intent, 1);  // Mã yêu cầu là 1
            }
        });
    }

    // Lấy dữ liệu từ Firebase
    private void loadTasksFromFirebase() {
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList.clear();  // Xóa danh sách cũ
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);  // Thêm công việc vào danh sách
                    }
                }
                taskAdapter.notifyDataSetChanged();  // Cập nhật Adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi
            }
        });
    }

    // Xử lý kết quả trả về từ AddNewTask
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Task task = (Task) data.getSerializableExtra("new_task");  // Nhận công việc mới
            if (task != null) {
                String taskId = taskRef.push().getKey();  // Tạo ID ngẫu nhiên cho công việc
                taskRef.child(taskId).setValue(task);  // Lưu công việc vào Firebase
            }
        }
    }
}
