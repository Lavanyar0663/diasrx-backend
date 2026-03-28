package com.simats.frontend;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityPharmacistPersonalInformationBinding;
import com.simats.frontend.network.SessionManager;

public class PharmacistPersonalInformationActivity extends AppCompatActivity {

    private ActivityPharmacistPersonalInformationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPharmacistPersonalInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Load specific user data from SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String userId = sessionManager.getUserId();

        // Ensure proper null-checks
        if (name != null && !name.isEmpty()) {
            binding.tvProfileName.setText(name);
            binding.tvFullName.setText(name);
        }
        
        if (email != null && !email.isEmpty()) {
            binding.tvEmail.setText(email);
        }
        
        if (userId != null && !userId.isEmpty()) {
            binding.tvEmployeeId.setText(userId);
        }

        // We leave the rest as mock data matching the screenshot since the API 
        // doesn't provide these specific fields yet. 
        // If they did, we would fetch them here just like name and email.
    }
}
