package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityResetPasswordBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.Editable;
import android.text.TextWatcher;
import androidx.core.content.ContextCompat;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private String email;
    private String otp;
    private boolean isPasswordValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = getIntent().getStringExtra("email");
        otp = getIntent().getStringExtra("otp");

        // Back button work properly
        binding.btnBack.setOnClickListener(v -> finish());
        
        // Interactive Password Validation
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordRules(s.toString());
            }
        });

        binding.btnReset.setOnClickListener(v -> {
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show();
            } else if (!isPasswordValid) {
                Toast.makeText(this, "Please securely fulfill all password rules", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(password);
            }
        });
    }

    private void validatePasswordRules(String password) {
        boolean hasLength = password.length() >= 8;
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasBothCases = password.matches(".*[A-Z].*") && password.matches(".*[a-z].*");

        updateRuleIcon(binding.tvRuleLength, hasLength);
        updateRuleIcon(binding.tvRuleSpecial, hasSpecial);
        updateRuleIcon(binding.tvRuleNumber, hasNumber);
        updateRuleIcon(binding.tvRuleCase, hasBothCases);

        isPasswordValid = hasLength && hasSpecial && hasNumber && hasBothCases;
    }

    private void updateRuleIcon(android.widget.TextView textView, boolean isValid) {
        int drawableRes = isValid ? R.drawable.ic_rule_checked : R.drawable.ic_rule_unchecked;
        textView.setCompoundDrawablesWithIntrinsicBounds(drawableRes, 0, 0, 0);
    }

    private void resetPassword(String password) {
        binding.btnReset.setEnabled(false);
        binding.btnReset.setText("Updating...");

        JsonObject resetData = new JsonObject();
        resetData.addProperty("email", email);
        resetData.addProperty("otp", otp);
        resetData.addProperty("newPassword", password);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.resetPassword(resetData);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.btnReset.setEnabled(true);
                binding.btnReset.setText("Reset Password");

                if (response.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, PasswordUpdatedActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Error resetting password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnReset.setEnabled(true);
                binding.btnReset.setText("Reset Password");
                Toast.makeText(ResetPasswordActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
