package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityPasswordUpdatedBinding;

public class PasswordUpdatedActivity extends AppCompatActivity {

    private ActivityPasswordUpdatedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordUpdatedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button navigation -> goes to Settings
        binding.btnBack.setOnClickListener(v -> navigateToSettings());
        
        // "Back to Settings" wide bottom button
        binding.btnBackToSettings.setOnClickListener(v -> navigateToSettings());
    }

    private void navigateToSettings() {
        Intent intent = new Intent(this, PharmacistProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateToSettings();
    }
}
