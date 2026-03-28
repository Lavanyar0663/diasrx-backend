package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityPatientRegistrationCompleteBinding;

public class PatientRegistrationCompleteActivity extends AppCompatActivity {

    private ActivityPatientRegistrationCompleteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientRegistrationCompleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get details from intent
        String name = getIntent().getStringExtra("PATIENT_NAME");
        String pid = getIntent().getStringExtra("PATIENT_ID");
        String mobile = getIntent().getStringExtra("PATIENT_MOBILE");

        binding.tvPatientName.setText(name != null ? name : "N/A");
        binding.tvPatientId.setText(pid != null ? pid : "N/A");
        binding.tvPatientMobile.setText(mobile != null ? mobile : "N/A");

        binding.btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(PatientRegistrationCompleteActivity.this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(PatientRegistrationCompleteActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
