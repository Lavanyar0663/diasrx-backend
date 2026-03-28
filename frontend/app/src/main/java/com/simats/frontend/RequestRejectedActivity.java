package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityRequestRejectedBinding;

public class RequestRejectedActivity extends AppCompatActivity {

    private ActivityRequestRejectedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestRejectedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String applicantName = getIntent().getStringExtra("applicant_name");
        if (applicantName != null && !applicantName.isEmpty()) {
            binding.tvDescription.setText("The access request from " + applicantName + " has been rejected and removed from system.");
        }

        binding.btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
