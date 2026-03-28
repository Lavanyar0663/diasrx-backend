package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityPrescriptionIssuedBinding;

public class PrescriptionIssuedActivity extends AppCompatActivity {

    private ActivityPrescriptionIssuedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionIssuedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String patientName = getIntent().getStringExtra("patient_name");
        if(patientName != null && !patientName.isEmpty()) {
            binding.tvPatientName.setText(patientName);
        }

        binding.btnBackDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DoctorMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
