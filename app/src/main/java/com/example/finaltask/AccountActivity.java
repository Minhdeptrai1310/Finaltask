package com.example.finaltask;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finaltask.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    private TextView tvUserEmail, tvUserId;
    private Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Ánh xạ view từ layout
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserId = findViewById(R.id.tvUserId);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Lấy thông tin người dùng từ FirebaseAuth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvUserEmail.setText("Email: " + user.getEmail());
            tvUserId.setText("User ID: " + user.getUid());
        }

        // Xử lý nút đăng xuất
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            // Chuyển về màn hình đăng nhập và xóa stack Activity cũ
            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
