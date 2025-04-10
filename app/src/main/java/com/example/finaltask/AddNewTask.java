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
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

        setupSpinner();
        etTaskDate.setOnClickListener(v -> showDateTimePicker());
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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo task và gán dữ liệu
        Task newTask = new Task();
        newTask.setTaskName(taskName);
        newTask.setTaskCategory(taskCategory);
        newTask.setTaskDateTime(taskDateTime);
        newTask.setUserId(currentUser.getUid());

        // Tạo danh sách sharedWithOrOwner gồm userId và email
        List<String> sharedWithOrOwner = new ArrayList<>();
        sharedWithOrOwner.add(currentUser.getUid());
        sharedWithOrOwner.add(currentUser.getEmail());
        newTask.setSharedWithOrOwner(sharedWithOrOwner);

        // Gửi task về MainActivity để thêm vào Firestore
        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_task", newTask);
        setResult(RESULT_OK, resultIntent);
        finish();

        setAlarm(calendar);
    }

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
