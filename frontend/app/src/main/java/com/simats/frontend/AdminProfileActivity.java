package com.simats.frontend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityAdminProfileBinding;
import com.simats.frontend.network.SessionManager;

public class AdminProfileActivity extends AppCompatActivity {

    private ActivityAdminProfileBinding binding;
    private SessionManager sessionManager;
    private boolean is2FAEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());

        // Open Edit Profile on Avatar click
        binding.ivProfileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        // Notifications
        binding.btnNotificationManager.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationMasterActivity.class);
            startActivity(intent);
        });

        // Security
        binding.btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // Two-Factor Auth 
        binding.switch2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            is2FAEnabled = isChecked;
            binding.tv2FAStatus.setText(isChecked ? "Enabled" : "Disabled");
            
            // Fix color when disabled
            updateSwitchColor(isChecked);
            
            Toast.makeText(this, "Two-Factor Auth " + (isChecked ? "Enabled" : "Disabled"), Toast.LENGTH_SHORT).show();
        });
        
        // Initial state color fix
        updateSwitchColor(is2FAEnabled);
        // App Versions
        binding.btnAppVersion.setOnClickListener(v -> 
            Toast.makeText(this, "Checking for updates...", Toast.LENGTH_SHORT).show()
        );

        // Logout
        binding.btnLogout.setOnClickListener(v -> {
            com.simats.frontend.utils.DialogHelper.showLogoutDialog(this, () -> {
                Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
                sessionManager.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    private void updateSwitchColor(boolean isChecked) {
        if (isChecked) {
            binding.switch2FA.setTrackTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.colorPrimary)));
            binding.switch2FA.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        } else {
            binding.switch2FA.setTrackTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
            binding.switch2FA.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
    }
}
