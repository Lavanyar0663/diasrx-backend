package com.simats.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityPharmacistProfileBinding;

public class PharmacistProfileActivity extends AppCompatActivity {

    private ActivityPharmacistProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPharmacistProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load stored user data from SessionManager
        com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(this);
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String userId = sessionManager.getUserId();

        binding.tvUserName.setText(name != null ? name : "Pharmacist");
        binding.tvUserEmail.setText(email != null ? email : "Not available");
        binding.tvUserId.setText("ID: " + (userId != null ? userId : "N/A"));

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnPersonalInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, PharmacistPersonalInformationActivity.class));
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });
                
        binding.btnNotificationSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });
                
        binding.btnAppLanguage.setOnClickListener(v ->
                Toast.makeText(this, "Language configuration coming soon", Toast.LENGTH_SHORT).show());

        // Logout button
        binding.btnLogout.setOnClickListener(v -> {
            com.simats.frontend.utils.DialogHelper.showLogoutDialog(this, () -> {
                // Clear session using SessionManager
                sessionManager.logout();
                
                // Go back to Login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
