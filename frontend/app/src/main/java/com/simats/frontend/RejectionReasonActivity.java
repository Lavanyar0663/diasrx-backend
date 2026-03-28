package com.simats.frontend;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.frontend.databinding.ActivityRejectionReasonBinding;

public class RejectionReasonActivity extends AppCompatActivity {

    private ActivityRejectionReasonBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRejectionReasonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get details from intent if passed dynamically
        String applicantName = getIntent().getStringExtra("applicant_name");
        String role = getIntent().getStringExtra("role");
        
        if (applicantName != null) {
            binding.tvName.setText(applicantName);
        }
        if (role != null) {
            binding.tvRole.setText(role.toUpperCase());
        }
        
        // Mock data logic 
        if ("Pharmacist".equalsIgnoreCase(role)) {
            binding.tvRole.setBackgroundResource(R.drawable.bg_badge_teal);
            binding.tvRole.setTextColor(android.graphics.Color.parseColor("#00897B"));
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBackToList.setOnClickListener(v -> finish());
        
        binding.tvReconsider.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Request reconsidered and moved back to pending", android.widget.Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
