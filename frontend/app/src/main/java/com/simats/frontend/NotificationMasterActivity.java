package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityNotificationMasterBinding;

public class NotificationMasterActivity extends AppCompatActivity {

    private ActivityNotificationMasterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationMasterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Save button
        binding.btnSave.setOnClickListener(v -> {
            // Simulated save logic
            Toast.makeText(this, "Global changes saved successfully!", Toast.LENGTH_SHORT).show();
            
            // Navigate to Success screen (Fig. 2)
            Intent intent = new Intent(this, NotificationSettingsUpdatedActivity.class);
            startActivity(intent);
            finish();
        });

        // Initialize switch listeners if needed for persistence later
        setupSwitchListeners();
    }

    private void setupSwitchListeners() {
        // Here we could add SharedPreferences logic for each switch
        // For now, they are visual and the "Save" button triggers the flow
    }
}
