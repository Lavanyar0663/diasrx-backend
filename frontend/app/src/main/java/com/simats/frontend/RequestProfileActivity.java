package com.simats.frontend;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityRequestProfileBinding;
import com.simats.frontend.models.AccessRequest;

public class RequestProfileActivity extends AppCompatActivity {

    private ActivityRequestProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Get Request Data
        AccessRequest request = (AccessRequest) getIntent().getSerializableExtra("request_data");

        if (request != null) {
            setupProfile(request);
        } else {
            Toast.makeText(this, "Error loading profile data", Toast.LENGTH_SHORT).show();
            finish();
        }

        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);

        // Actions
        binding.btnProfileApprove.setOnClickListener(v -> {
            if (request == null) return;
            apiInterface.approveRequest(request.getId()).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (response.isSuccessful()) {
                        android.content.Intent approveIntent = new android.content.Intent(RequestProfileActivity.this, RequestApprovedActivity.class);
                        approveIntent.putExtra("applicant_name", request.getName());
                        startActivity(approveIntent);
                        finish();
                    } else {
                        Toast.makeText(RequestProfileActivity.this, "Approval failed", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                    Toast.makeText(RequestProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnProfileReject.setOnClickListener(v -> {
            if (request == null) return;
            apiInterface.rejectRequest(request.getId()).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (response.isSuccessful()) {
                        android.content.Intent rejectIntent = new android.content.Intent(RequestProfileActivity.this, RejectionReasonActivity.class);
                        rejectIntent.putExtra("applicant_name", request.getName());
                        rejectIntent.putExtra("role", request.getRole());
                        startActivity(rejectIntent);
                        finish();
                    } else {
                        Toast.makeText(RequestProfileActivity.this, "Rejection failed", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                    Toast.makeText(RequestProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupProfile(AccessRequest request) {
        // Core Info
        binding.tvProfileName.setText(request.getName());
        binding.tvProfileRole.setText(request.getRole().toUpperCase());
        binding.tvProfilePhone.setText(request.getPhone());

        // Submission Summary (New Section)
        binding.tvSummaryName.setText(request.getName());
        binding.tvSummaryRole.setText(request.getRole() + " (" + request.getDepartment() + ")");
        
        // Mocking a date since AccessRequest model might just have timeAgo
        String displayDate = request.getTimeAgo().contains("ago") ? "Oct 24, 2023" : request.getTimeAgo();
        binding.tvSummaryDate.setText(displayDate);

        // Role Badge Color
        if (request.getRole().equalsIgnoreCase("Pharmacist")) {
            binding.tvProfileRole.setBackgroundResource(R.drawable.bg_badge_teal);
            binding.tvProfileRole.setTextColor(Color.parseColor("#00897B"));
        } else {
            binding.tvProfileRole.setBackgroundResource(R.drawable.bg_badge_blue);
            binding.tvProfileRole.setTextColor(Color.parseColor("#0052CC"));
        }
    }
}
