package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityNewStaffBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewStaffActivity extends AppCompatActivity {

    private ActivityNewStaffBinding binding;

    private static final String[] DEPARTMENTS = {
        "Oral Surgery", "Oral Medicine", "Orthodontics", "Periodontics",
        "Prosthodontics", "Pediatric Dentistry", "Endodontics", "General Dentistry"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewStaffBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup Role Dropdown
        String[] roles = new String[]{"Doctor", "Pharmacist", "Admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        binding.tvRoleDropdown.setAdapter(roleAdapter);

        // Setup Department Dropdown
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, DEPARTMENTS);
        if (binding.tvDeptDropdown != null) {
            binding.tvDeptDropdown.setAdapter(deptAdapter);
        }

        // Show/hide department based on role
        binding.tvRoleDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String role = roles[position];
            if (binding.tilDeptDropdown != null) {
                binding.tilDeptDropdown.setVisibility(
                    role.equalsIgnoreCase("Doctor") ? View.VISIBLE : View.GONE
                );
            }
        });

        binding.btnRegister.setOnClickListener(v -> registerStaff());
    }

    private void registerStaff() {
        String fullName = binding.etFullName.getText().toString().trim();
        String mobile = binding.etMobileNumber.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String selectedRole = binding.tvRoleDropdown.getText().toString().trim().toLowerCase();

        if (fullName.isEmpty() || mobile.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mobile.length() < 10) {
            Toast.makeText(this, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get department (required for doctors)
        String department = "";
        if (binding.tvDeptDropdown != null) {
            department = binding.tvDeptDropdown.getText().toString().trim();
        }
        if (selectedRole.equals("doctor") && department.isEmpty()) {
            Toast.makeText(this, "Please select a department for the doctor", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", fullName);
        requestBody.addProperty("password", password);
        requestBody.addProperty("role", selectedRole);
        requestBody.addProperty("phone", mobile);
        if (!department.isEmpty()) {
            requestBody.addProperty("department", department);
        }

        Log.d("NewStaffActivity", "Registering: " + requestBody.toString());

        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Registering...");

        Call<JsonObject> call = apiInterface.registerUser(requestBody);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("Register User");

                if (response.isSuccessful() && response.body() != null) {
                    String finalEmail = "";
                    if (response.body().has("email") && !response.body().get("email").isJsonNull()) {
                        finalEmail = response.body().get("email").getAsString();
                    }

                    // Navigate to success screen
                    Intent intent = new Intent(NewStaffActivity.this, StaffRegistrationCompleteActivity.class);
                    intent.putExtra("STAFF_NAME", fullName);
                    intent.putExtra("STAFF_ROLE", selectedRole.substring(0, 1).toUpperCase() + selectedRole.substring(1));
                    intent.putExtra("STAFF_MOBILE", mobile);
                    intent.putExtra("STAFF_EMAIL", finalEmail);
                    startActivity(intent);
                    finish();
                } else {
                    // Show actual server error
                    String errorMsg = "Registration failed (Code: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject errObj = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (errObj.has("message")) errorMsg = errObj.get("message").getAsString();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    Log.e("NewStaffActivity", "Error " + response.code() + ": " + errorMsg);
                    Toast.makeText(NewStaffActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("Register User");
                Toast.makeText(NewStaffActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
