package com.example.finaltask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.finaltask.notification.AlarmReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);
    }

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

    private void updateDateTimeDisplay() {
        String dateTimeFormat = "dd MMMM yyyy HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat, Locale.getDefault());
        etTaskDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveTaskToFirestore() {
        String taskName = etTaskName.getText().toString().trim();
        String taskCategory = spCategory.getSelectedItem().toString();
        String taskDateTime = new SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
                .format(calendar.getTime());

        if (taskName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy userId từ Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tạo task object và gán giá trị
        Task newTask = new Task();
        newTask.setTaskName(taskName);
        newTask.setTaskCategory(taskCategory);
        newTask.setTaskDateTime(taskDateTime);
        newTask.setUserId(userId);

        // Thêm task vào Firestore và nhận documentId
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tasks")
                .add(newTask)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    setAlarm(calendar, documentId, taskName); // Truyền documentId và taskName

                    // Trả kết quả về MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("new_task", newTask);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu công việc", Toast.LENGTH_SHORT).show();
                });
    }

    private void setAlarm(Calendar calendar, String documentId, String taskName) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("documentId", documentId);
        intent.putExtra("taskName", taskName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                documentId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Thông báo đã được cài đặt!", Toast.LENGTH_SHORT).show();
        }
    }
}