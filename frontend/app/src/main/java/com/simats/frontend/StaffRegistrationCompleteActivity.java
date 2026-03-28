package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityStaffRegistrationCompleteBinding;

public class StaffRegistrationCompleteActivity extends AppCompatActivity {

    private ActivityStaffRegistrationCompleteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffRegistrationCompleteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get details from intent
        String name = getIntent().getStringExtra("STAFF_NAME");
        String role = getIntent().getStringExtra("STAFF_ROLE");
        String mobile = getIntent().getStringExtra("STAFF_MOBILE");

        binding.tvStaffName.setText(name != null ? name : "N/A");
        binding.tvStaffRole.setText(role != null ? role : "N/A");
        binding.tvStaffMobile.setText(mobile != null ? mobile : "N/A");

        binding.btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(StaffRegistrationCompleteActivity.this, AdminDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(StaffRegistrationCompleteActivity.this, AdminDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }


}
