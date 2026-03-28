package com.simats.frontend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityChangePasswordBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private ApiInterface apiInterface;

    // Strength levels
    private static final int STRENGTH_NONE   = 0;
    private static final int STRENGTH_WEAK   = 1;
    private static final int STRENGTH_FAIR   = 2;
    private static final int STRENGTH_GOOD   = 3;
    private static final int STRENGTH_STRONG = 4;

    private int currentStrength = STRENGTH_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        apiInterface = ApiClient.getClient(this).create(ApiInterface.class);

        // Dynamic password strength watcher
        binding.etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateStrengthUI(s.toString());
            }
        });

        binding.btnUpdatePassword.setOnClickListener(v -> handlePasswordUpdate());
    }

    private void updateStrengthUI(String password) {
        currentStrength = calculateStrength(password);

        // Colors
        final String COLOR_EMPTY  = "#E2E8F0";
        final String COLOR_WEAK   = "#EF4444"; // red
        final String COLOR_FAIR   = "#F59E0B"; // amber
        final String COLOR_GOOD   = "#3B82F6"; // blue
        final String COLOR_STRONG = "#0D9488"; // teal

        String fillColor;
        String label;

        switch (currentStrength) {
            case STRENGTH_NONE:
                fillColor = COLOR_EMPTY;
                label = "Enter a password";
                binding.tvStrengthLabel.setTextColor(Color.parseColor("#94A3B8"));
                break;
            case STRENGTH_WEAK:
                fillColor = COLOR_WEAK;
                label = "Weak password";
                binding.tvStrengthLabel.setTextColor(Color.parseColor(COLOR_WEAK));
                break;
            case STRENGTH_FAIR:
                fillColor = COLOR_FAIR;
                label = "Fair password";
                binding.tvStrengthLabel.setTextColor(Color.parseColor(COLOR_FAIR));
                break;
            case STRENGTH_GOOD:
                fillColor = COLOR_GOOD;
                label = "Good password";
                binding.tvStrengthLabel.setTextColor(Color.parseColor(COLOR_GOOD));
                break;
            default: // STRONG
                fillColor = COLOR_STRONG;
                label = "Strong password ✓";
                binding.tvStrengthLabel.setTextColor(Color.parseColor(COLOR_STRONG));
                break;
        }

        binding.tvStrengthLabel.setText(label);

        // Update bars: fill according to strength level
        binding.strengthBar1.setBackgroundColor(
                currentStrength >= 1 ? Color.parseColor(fillColor) : Color.parseColor(COLOR_EMPTY));
        binding.strengthBar2.setBackgroundColor(
                currentStrength >= 2 ? Color.parseColor(fillColor) : Color.parseColor(COLOR_EMPTY));
        binding.strengthBar3.setBackgroundColor(
                currentStrength >= 3 ? Color.parseColor(fillColor) : Color.parseColor(COLOR_EMPTY));
        binding.strengthBar4.setBackgroundColor(
                currentStrength >= 4 ? Color.parseColor(fillColor) : Color.parseColor(COLOR_EMPTY));
    }

    /**
     * Returns 0–4 strength score based on:
     * length ≥ 6: +1
     * has uppercase: +1
     * has digit: +1
     * has special char: +1
     */
    private int calculateStrength(String password) {
        if (password.isEmpty()) return STRENGTH_NONE;
        int score = 0;
        if (password.length() >= 6)                        score++;
        if (password.matches(".*[A-Z].*"))                 score++;
        if (password.matches(".*[0-9].*"))                 score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:',.<>?/`~].*")) score++;
        return score;
    }

    private void handlePasswordUpdate() {
        String currentPass = binding.etCurrentPassword.getText() != null
                ? binding.etCurrentPassword.getText().toString().trim() : "";
        String newPass     = binding.etNewPassword.getText() != null
                ? binding.etNewPassword.getText().toString().trim() : "";
        String confirmPass = binding.etConfirmPassword.getText() != null
                ? binding.etConfirmPassword.getText().toString().trim() : "";

        // All fields required
        if (currentPass.isEmpty()) {
            binding.tilCurrentPassword.setError("Current password is required");
            return;
        }
        binding.tilCurrentPassword.setError(null);

        if (newPass.isEmpty()) {
            binding.tilNewPassword.setError("New password is required");
            return;
        }
        binding.tilNewPassword.setError(null);

        // Must not be same as current
        if (currentPass.equals(newPass)) {
            binding.tilNewPassword.setError("New password must be different from current password");
            return;
        }
        binding.tilNewPassword.setError(null);

        // Must be strong (all 4 bars filled)
        if (currentStrength < STRENGTH_STRONG) {
            binding.tilNewPassword.setError("Please use a stronger password (uppercase, number, special character)");
            return;
        }
        binding.tilNewPassword.setError(null);

        // Confirm must match
        if (!newPass.equals(confirmPass)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }
        binding.tilConfirmPassword.setError(null);

        // All validations passed — call API
        JsonObject body = new JsonObject();
        body.addProperty("currentPassword", currentPass);
        body.addProperty("newPassword", newPass);

        binding.btnUpdatePassword.setEnabled(false);
        binding.btnUpdatePassword.setText("Updating...");

        // Simulate API call success for UI flow
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            binding.btnUpdatePassword.setEnabled(true);
            binding.btnUpdatePassword.setText("Update Password");
            Intent intent = new Intent(ChangePasswordActivity.this, PasswordUpdatedActivity.class);
            startActivity(intent);
            finish();
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
