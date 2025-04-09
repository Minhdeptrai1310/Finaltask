package com.example.finaltask;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.finaltask.notification.AlarmReceiver;

public class AddNewTask extends Activity {

    private EditText etTaskName, etTaskDate;
    private Spinner spCategory;
    private Button btnSave;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_task);

        // Ánh xạ view
        etTaskName = findViewById(R.id.etTaskName);
        etTaskDate = findViewById(R.id.etTaskDate);
        spCategory = findViewById(R.id.spinnerTaskCategory);
        btnSave = findViewById(R.id.btnSave);

        calendar = Calendar.getInstance();

        // Thiết lập Spinner với danh sách category
        setupSpinner();

        // Xử lý chọn ngày/giờ
        etTaskDate.setOnClickListener(v -> showDateTimePicker());

        // Xử lý lưu task
        btnSave.setOnClickListener(v -> saveTaskToFirestore());
    }

    // Thiết lập Spinner từ mảng string resource
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_categories, // Đảm bảo mảng này khớp với Firestore
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

    // Hiển thị dialog chọn ngày và giờ
    private void showDateTimePicker() {
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                updateDateTimeDisplay();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // Cập nhật hiển thị ngày giờ đã chọn
    private void updateDateTimeDisplay() {
        String dateTimeFormat = "dd MMMM yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat, Locale.getDefault());
        etTaskDate.setText(sdf.format(calendar.getTime()));
    }

    // Lưu task vào Firestore và cài đặt thông báo
    private void saveTaskToFirestore() {
        String taskName = etTaskName.getText().toString().trim();
        String taskCategory = spCategory.getSelectedItem().toString(); // Lấy category từ Spinner
        String taskDateTime = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
                .format(calendar.getTime());

        // Validate dữ liệu
        if (taskName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo task object và gán giá trị
        Task newTask = new Task();
        newTask.setTaskName(taskName);
        newTask.setTaskCategory(taskCategory); // Lưu category đúng định dạng
        newTask.setTaskDateTime(taskDateTime);

        // Trả kết quả về MainActivity (nếu cần)
        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_task", newTask);
        setResult(RESULT_OK, resultIntent);
        finish();

        // Cài đặt thông báo
        setAlarm(calendar);
    }

    // Cài đặt AlarmManager để hiển thị thông báo khi đến giờ
    private void setAlarm(Calendar calendar) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
            Toast.makeText(this, "Thông báo đã được cài đặt!", Toast.LENGTH_SHORT).show();
        }
    }
}