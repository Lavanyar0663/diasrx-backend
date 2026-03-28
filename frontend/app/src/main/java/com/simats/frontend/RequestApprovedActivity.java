package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityRequestApprovedBinding;

public class RequestApprovedActivity extends AppCompatActivity {

    private ActivityRequestApprovedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestApprovedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String applicantName = getIntent().getStringExtra("applicant_name");
        if (applicantName == null || applicantName.isEmpty()) {
            applicantName = "The user";
        }

        String description = applicantName
                + " has been successfully added to the system. They will receive a notification to log in.";
        binding.tvDescription.setText(description);

        binding.btnBackToDashboard.setOnClickListener(v -> {
            // Navigate back to Admin Dashboard and clear top activities
            Intent intent = new Intent(this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
