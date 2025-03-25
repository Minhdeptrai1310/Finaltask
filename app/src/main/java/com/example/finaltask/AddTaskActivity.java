package com.example.finaltask;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AddTaskActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Tìm TextView theo ID
        TextView tvSelectedDate = findViewById(R.id.tvSelectedDate);

        // Nhận ngày từ Intent
        String selectedDate = getIntent().getStringExtra("selectedDate");

        // Kiểm tra nếu selectedDate không null trước khi setText
        if (selectedDate != null) {
            tvSelectedDate.setText(getString(R.string.selected_date, selectedDate));
        }
    }
}
