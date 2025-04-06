package com.example.finaltask.login;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finaltask.R;

public class ForgetActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        // Khởi tạo các view
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Đặt sự kiện cho nút gửi yêu cầu reset mật khẩu
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(ForgetActivity.this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();
                } else {
                    // Xử lý yêu cầu đặt lại mật khẩu
                    // Bạn có thể thêm logic để xử lý gửi email reset mật khẩu ở đây

                    Toast.makeText(ForgetActivity.this, "Yêu cầu đặt lại mật khẩu đã được gửi", Toast.LENGTH_SHORT).show();
                    // Quay lại màn hình đăng nhập
                    startActivity(new Intent(ForgetActivity.this, LoginActivity.class));
                }
            }
        });

        // Đặt sự kiện cho TextView quay lại trang đăng nhập
        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình đăng nhập
                startActivity(new Intent(ForgetActivity.this, LoginActivity.class));
            }
        });
    }
}