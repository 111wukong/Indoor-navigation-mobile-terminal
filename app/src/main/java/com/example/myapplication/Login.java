package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

    }
    boolean false1 = false;
    public void Reset(View view) {
        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        if (username.getText().toString().equals("admin") && password.getText().toString().equals("123456")){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "恭喜您，登录成功！", Toast.LENGTH_SHORT).show();
            false1 = true;
        }
        else {
            Toast.makeText(this, "用户名或密码错误！", Toast.LENGTH_SHORT).show();
            username.setText("");
            password.setText("");
            false1 =false;
        }
    }
    public void Clear(View view) {
        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        username.setText("");
        password.setText("");
        Toast.makeText(this, "重置成功", Toast.LENGTH_SHORT).show();
    }



}

