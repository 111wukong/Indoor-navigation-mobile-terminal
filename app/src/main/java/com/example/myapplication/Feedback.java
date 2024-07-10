package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Feedback extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
    }

    public void Reset1(View view) {
        TextView text = findViewById(R.id.feedbacktext);
        if (!text.equals("")){
            Toast.makeText(this, "提交成功，感谢您的建议！", Toast.LENGTH_SHORT).show();
            text.setText("");
        }
        else {
            Toast.makeText(this, "提交失败，文本不能为空！", Toast.LENGTH_SHORT).show();
        }

    }

    public void Clear1(View view) {
        TextView text = findViewById(R.id.feedbacktext);
        text.setText("");
        Toast.makeText(this, "重置成功！", Toast.LENGTH_SHORT).show();
    }
}
