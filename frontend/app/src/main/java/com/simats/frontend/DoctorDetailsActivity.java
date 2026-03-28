package com.simats.frontend;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.frontend.databinding.ActivityDoctorDetailsBinding;

public class DoctorDetailsActivity extends AppCompatActivity {

    private ActivityDoctorDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        com.simats.frontend.models.Doctor doctor = (com.simats.frontend.models.Doctor) getIntent().getSerializableExtra("doctor");
        
        binding.btnMenu.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            popup.getMenu().add("Remove Doctor");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Remove Doctor")) {
                    showRemoveConfirmation(doctor);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        if (doctor != null) {
            binding.tvHeaderTitle.setText("Doctor Details");
            binding.tvDoctorName.setText(doctor.getName());
            binding.tvStatusBadge.setText(doctor.getStatus().toUpperCase());
            binding.tvConsultantLabel.setText(doctor.getSpecialization());
            binding.tvDoctorId.setText(doctor.getId());
            binding.tvDepartment.setText(doctor.getDepartment());
            binding.tvSpecialization.setText(doctor.getSpecialization());
            binding.tvExperience.setText(doctor.getExperience());
            binding.tvMobileNumber.setText(doctor.getMobile());
            binding.tvEmail.setText(doctor.getEmail());

            if (doctor.getStatus().equalsIgnoreCase("ACTIVE")) {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);
                binding.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#00897B"));
            } else {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_grey);
                binding.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#5A6B82"));
            }
        }
    }

    private void showRemoveConfirmation(com.simats.frontend.models.Doctor doctor) {
        String name = (doctor != null ? doctor.getName() : "this doctor");
        com.simats.frontend.utils.DialogHelper.showConfirmationDialog(
                this,
                R.drawable.ic_logout, // Generic warning icon
                "Remove Doctor",
                "Are you sure you want to remove " + name + " from the system?",
                "Remove",
                "Cancel",
                () -> removeDoctor(doctor)
        );
    }

    private void removeDoctor(com.simats.frontend.models.Doctor doctor) {
        android.content.SharedPreferences prefs = getSharedPreferences("AdminStats", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        // Decrement count
        int count = prefs.getInt("doctor_count", 46); // Using 46 as default based on dashboard UI
        editor.putInt("doctor_count", Math.max(0, count - 1));
        
        // Remove from dynamic list if present
        if (doctor != null) {
            String newDocs = prefs.getString("new_doctors_data", "");
            if (!newDocs.isEmpty()) {
                String[] docs = newDocs.split(";");
                StringBuilder updatedDocs = new StringBuilder();
                boolean removed = false;
                for (String d : docs) {
                    if (d.contains(doctor.getName()) && d.contains(doctor.getMobile())) {
                        removed = true;
                        continue; // Skip this one
                    }
                    if (updatedDocs.length() > 0) updatedDocs.append(";");
                    updatedDocs.append(d);
                }
                editor.putString("new_doctors_data", updatedDocs.toString());
            }
        }
        
        editor.apply();
        Toast.makeText(this, "User removed successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
