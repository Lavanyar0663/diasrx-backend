package com.simats.frontend;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.frontend.databinding.ActivityAccessManagementBinding;

public class AccessManagementActivity extends AppCompatActivity {
    private ActivityAccessManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccessManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnApply.setOnClickListener(v -> {
            Toast.makeText(this, "Settings applied successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
