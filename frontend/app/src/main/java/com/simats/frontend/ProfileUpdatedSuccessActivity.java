package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityProfileUpdatedSuccessBinding;

public class ProfileUpdatedSuccessActivity extends AppCompatActivity {

    private ActivityProfileUpdatedSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileUpdatedSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Both back arrow and "Back to Profile" button navigate to Settings
        binding.toolbar.setNavigationOnClickListener(v -> navigateBack());
        binding.btnBackToProfile.setOnClickListener(v -> navigateBack());
    }

    private void navigateBack() {
        // Go back to DoctorSettingsActivity, clearing anything on top of it
        Intent intent = new Intent(this, DoctorSettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }
}
