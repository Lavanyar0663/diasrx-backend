package com.simats.frontend;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityNotificationSettingsUpdatedBinding;

public class NotificationSettingsUpdatedActivity extends AppCompatActivity {

    private ActivityNotificationSettingsUpdatedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsUpdatedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back to Settings button
        binding.btnBackToSettings.setOnClickListener(v -> finish());
    }
}
