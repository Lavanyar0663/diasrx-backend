package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityRequestSubmittedBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RequestSubmittedActivity extends AppCompatActivity {

    private ActivityRequestSubmittedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestSubmittedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get Data
        String name = getIntent().getStringExtra("name");
        String role = getIntent().getStringExtra("role");
        String dept = getIntent().getStringExtra("dept");

        // Display Data
        binding.tvSummaryName.setText(name);
        binding.tvSummaryRole.setText(dept != null && !dept.isEmpty() ? role + " (" + dept + ")" : role);
        
        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        binding.tvSummaryDate.setText(currentDate);

        // Navigation
        binding.btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to registration form
        super.onBackPressed();
        binding.btnBackToLogin.performClick();
    }
}
