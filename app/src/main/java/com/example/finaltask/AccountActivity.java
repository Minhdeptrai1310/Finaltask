package com.example.finaltask;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finaltask.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUserEmail;
    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);  // Layout cho AccountActivity

        // Ánh xạ các view
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Lấy email người dùng đã đăng nhập từ FirebaseAuth
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        tvUserEmail.setText(userEmail);

        // Xử lý sự kiện đăng xuất
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // Chuyển đến LoginActivity và xóa stack Activity cũ
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa toàn bộ stack
            startActivity(intent);
            finish(); // Đóng AccountActivity
        });
    }
}
