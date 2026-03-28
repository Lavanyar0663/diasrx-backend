package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityRequestAccessBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestAccessActivity extends AppCompatActivity {

    private ActivityRequestAccessBinding binding;
    private String selectedRole = "Doctor"; // default
    private final String[] departments = new String[] {
        "Oral Surgery", "Oral Medicine", "Orthodontics", "Periodontics",
        "Prosthodontics", "Pediatric Dentistry", "Endodontics", "General Dentistry"
    };
    private final String[] pharmacies = new String[] {
            "Main Pharmacy - Block A", "OPD Pharmacy - Block B", "Emergency Pharmacy", "Dental Block Pharmacy", "Staff Clinic Pharmacy"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestAccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Dropdown for Department
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                departments);
        binding.actvDepartment.setAdapter(deptAdapter);

        // Setup Dropdown for Pharmacy
        ArrayAdapter<String> pharmAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                pharmacies);
        binding.actvPharmacy.setAdapter(pharmAdapter);

        // Initial UI state (Doctor Selected)
        updateRoleSelection(binding.cardDoctor, binding.tvDoctorText, binding.ivDoctorIcon);

        // Click Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Role Toggle
        binding.cardDoctor.setOnClickListener(v -> {
            selectedRole = "Doctor";
            updateRoleSelection(binding.cardDoctor, binding.tvDoctorText, binding.ivDoctorIcon);
        });

        binding.cardPharmacist.setOnClickListener(v -> {
            selectedRole = "Pharmacist";
            updateRoleSelection(binding.cardPharmacist, binding.tvPharmText, binding.ivPharmIcon);
        });

        // Submit Logic
        binding.btnSubmit.setOnClickListener(v -> handleSubmission());
    }

    private void updateRoleSelection(MaterialCardView selectedCard, android.widget.TextView selectedText,
            android.widget.ImageView selectedIcon) {
        // Reset both cards
        resetCard(binding.cardDoctor, binding.tvDoctorText, binding.ivDoctorIcon);
        resetCard(binding.cardPharmacist, binding.tvPharmText, binding.ivPharmIcon);

        // Highlight selected
        selectedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        selectedCard.setStrokeWidth(0);
        selectedText.setTextColor(ContextCompat.getColor(this, R.color.white));
        selectedIcon.setColorFilter(ContextCompat.getColor(this, R.color.white));

        // Toggle visibility based on role
        if (selectedRole.equals("Doctor")) {
            binding.tvDepartmentHeader.setVisibility(View.VISIBLE);
            binding.tilDepartment.setVisibility(View.VISIBLE);
            binding.tilProfessionalTitle.setVisibility(View.VISIBLE);
            binding.tvPharmacyHeader.setVisibility(View.GONE);
            binding.tilPharmacy.setVisibility(View.GONE);
        } else {
            binding.tvDepartmentHeader.setVisibility(View.GONE);
            binding.tilDepartment.setVisibility(View.GONE);
            binding.tilProfessionalTitle.setVisibility(View.GONE);
            binding.tvPharmacyHeader.setVisibility(View.VISIBLE);
            binding.tilPharmacy.setVisibility(View.VISIBLE);
        }
    }

    private void resetCard(MaterialCardView card, android.widget.TextView text, android.widget.ImageView icon) {
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.colorCardUnselectedBg));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.colorOutline));
        card.setStrokeWidth(2);
        text.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecondary));
        icon.setColorFilter(ContextCompat.getColor(this, R.color.colorTextSecondary));
    }

    private void handleSubmission() {
        String name = binding.etFullName.getText() != null ? binding.etFullName.getText().toString() : "";
        String mobile = binding.etMobile.getText() != null ? binding.etMobile.getText().toString() : "";
        String pwd = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";

        if (name.isEmpty() || mobile.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Please fill out name, mobile, and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.length() < 10) {
            Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pwd.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        String dept = "";
        String pharmacy = "";
        String title = "";

        if (selectedRole.equals("Doctor")) {
            dept = binding.actvDepartment.getText().toString();
            title = binding.etProfessionalTitle.getText() != null ? binding.etProfessionalTitle.getText().toString().trim() : "";
            if (dept.isEmpty()) {
                Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
                return;
            }
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter your professional title (e.g. Junior Doctor)", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (selectedRole.equals("Pharmacist")) {
            pharmacy = binding.actvPharmacy.getText().toString();
            if (pharmacy.isEmpty()) {
                Toast.makeText(this, "Please select a pharmacy", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Call API
        binding.btnSubmit.setEnabled(false);
        binding.btnSubmit.setText("Submitting...");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", name);
        requestBody.addProperty("phone", mobile);
        requestBody.addProperty("password", pwd);
        requestBody.addProperty("role", selectedRole.toLowerCase());
        
        if (selectedRole.equalsIgnoreCase("Doctor")) {
            requestBody.addProperty("department", dept);
            requestBody.addProperty("professional_title", title);
        } else if (selectedRole.equalsIgnoreCase("Pharmacist")) {
            requestBody.addProperty("pharmacy", pharmacy);
        }

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        apiInterface.requestAccess(requestBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.btnSubmit.setEnabled(true);
                binding.btnSubmit.setText("Submit Request");

                if (response.isSuccessful()) {
                    Toast.makeText(RequestAccessActivity.this, "Request submitted successfully!", Toast.LENGTH_LONG).show();
                    // Navigate to Success Screen
                    Intent intent = new Intent(RequestAccessActivity.this, RequestSubmittedActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("role", selectedRole);
                    if (selectedRole.equals("Doctor")) {
                        intent.putExtra("dept", binding.actvDepartment.getText().toString());
                    } else if (selectedRole.equals("Pharmacist")) {
                        intent.putExtra("pharmacy", binding.actvPharmacy.getText().toString());
                    }
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Submission failed";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject errorObj = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (errorObj.has("message")) errorMsg = errorObj.get("message").getAsString();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(RequestAccessActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnSubmit.setEnabled(true);
                binding.btnSubmit.setText("Submit Request");
                Toast.makeText(RequestAccessActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
