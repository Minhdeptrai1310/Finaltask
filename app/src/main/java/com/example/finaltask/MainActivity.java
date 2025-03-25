package com.example.finaltask;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ TextView hiển thị ngày tháng
        TextView tvDate = findViewById(R.id.tvDate);
        FloatingActionButton btnAddTask = findViewById(R.id.btnAddTask);

        // Lấy ngày hiện tại
        String currentDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("vi"))
                .format(Calendar.getInstance().getTime());

        // Cập nhật TextView với ngày hiện tại
        tvDate.setText("Hôm nay, " + currentDate);

        // Xử lý insets để tránh bị che khuất
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rvTask), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý khi nhấn nút thêm công việc
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;

                // Mở màn hình thêm công việc và truyền ngày đã chọn
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putExtra("selectedDate", selectedDate);
                startActivity(intent);
            }
        }, year, month, day);

        datePickerDialog.show();
    }
}
