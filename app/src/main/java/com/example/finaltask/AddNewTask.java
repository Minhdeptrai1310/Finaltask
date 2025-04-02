package com.example.finaltask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class AddNewTask extends Activity {

    private EditText etTaskName, etTaskDate;
    private Spinner spCategory;
    private Button btnSave;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_task);

        etTaskName = findViewById(R.id.etTaskName);
        etTaskDate = findViewById(R.id.etTaskDate);
        spCategory = findViewById(R.id.spinnerTaskCategory);  // Spinner để chọn loại công việc
        btnSave = findViewById(R.id.btnSave);

        calendar = Calendar.getInstance();

        // Tạo ArrayAdapter cho Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.task_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // Set listener cho chọn ngày và giờ
        etTaskDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hiển thị DatePicker để chọn ngày
                new DatePickerDialog(AddNewTask.this, dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Lưu công việc
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = etTaskName.getText().toString();
                String taskCategory = spCategory.getSelectedItem().toString();
                String taskDateTime = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
                        .format(calendar.getTime());

                if (taskName.isEmpty()) {
                    Toast.makeText(AddNewTask.this, "Tên công việc không được để trống!", Toast.LENGTH_SHORT).show();
                } else {
                    // Tạo đối tượng Task mới
                    Task newTask = new Task(taskName, taskDateTime, taskCategory);

                    // Gửi dữ liệu về MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("new_task", newTask); // Trả về công việc mới
                    setResult(RESULT_OK, resultIntent);

                    finish();  // Đóng Activity AddNewTask và quay lại MainActivity
                }
            }
        });
    }

    // Listener cho việc chọn ngày
    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Hiển thị TimePicker để chọn giờ
            new TimePickerDialog(AddNewTask.this, timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true).show();
        }
    };

    // Listener cho việc chọn giờ
    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            // Hiển thị ngày và giờ đã chọn
            String selectedDateTime = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault()).format(calendar.getTime());
            etTaskDate.setText(selectedDateTime);  // Hiển thị ngày và giờ đã chọn
        }
    };
}
