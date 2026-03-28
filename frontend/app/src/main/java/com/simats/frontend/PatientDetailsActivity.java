package com.simats.frontend;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.frontend.databinding.ActivityPatientDetailsBinding;

public class PatientDetailsActivity extends AppCompatActivity {

    private ActivityPatientDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        com.simats.frontend.models.Patient patient = (com.simats.frontend.models.Patient) getIntent().getSerializableExtra("patient");
        
        binding.btnMenu.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            popup.getMenu().add("Remove Patient");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Remove Patient")) {
                    showRemoveConfirmation(patient);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        if (patient != null) {
            binding.tvPatientName.setText(patient.getName());
            binding.tvPatientIdBadge.setText(patient.getPid());
            binding.tvStatusBadge.setText("Active"); // Defaulting to Active for now
            binding.tvAge.setText(patient.getAge());
            binding.tvGender.setText(patient.getGender());
            binding.tvBloodGroup.setText(patient.getBloodGroup());
            binding.tvDob.setText(patient.getDob());
            binding.tvMobile.setText(patient.getMobile());
            binding.tvEmail.setText(patient.getEmail());
            binding.tvAddress.setText(patient.getAddress());
            binding.tvDepartment.setText(patient.getDepartmentBadge());
            binding.tvDoctorName.setText(patient.getAssignedDoctor());
            binding.tvRegDate.setText(patient.getRegDate());
        }
    }

    private void showRemoveConfirmation(com.simats.frontend.models.Patient patient) {
        String name = (patient != null ? patient.getName() : "this patient");
        com.simats.frontend.utils.DialogHelper.showConfirmationDialog(
                this,
                R.drawable.ic_logout, // Generic warning icon
                "Remove Patient",
                "Are you sure you want to remove " + name + " from the system?",
                "Remove",
                "Cancel",
                () -> removePatient(patient)
        );
    }

    private void removePatient(com.simats.frontend.models.Patient patient) {
        android.content.SharedPreferences prefs = getSharedPreferences("AdminStats", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        // Decrement count
        int count = prefs.getInt("patient_count", 1240);
        editor.putInt("patient_count", Math.max(0, count - 1));
        
        // Remove from dynamic list if present
        if (patient != null) {
            String newPatients = prefs.getString("new_patients_data", "");
            if (!newPatients.isEmpty()) {
                String[] patients = newPatients.split(";");
                StringBuilder updatedPatients = new StringBuilder();
                for (String p : patients) {
                    if (p.contains(patient.getName()) && (p.contains(patient.getPid()) || p.contains(patient.getMobile()))) {
                        continue; // Skip this one
                    }
                    if (updatedPatients.length() > 0) updatedPatients.append(";");
                    updatedPatients.append(p);
                }
                editor.putString("new_patients_data", updatedPatients.toString());
            }
        }
        
        editor.apply();
        android.widget.Toast.makeText(this, "User removed successfully", android.widget.Toast.LENGTH_SHORT).show();
        finish();
    }
}
