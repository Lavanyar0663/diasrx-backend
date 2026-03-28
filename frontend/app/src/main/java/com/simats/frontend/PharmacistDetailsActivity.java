package com.simats.frontend;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.simats.frontend.databinding.ActivityPharmacistDetailsBinding;
import com.simats.frontend.models.Pharmacist;

public class PharmacistDetailsActivity extends AppCompatActivity {

    private ActivityPharmacistDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPharmacistDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        Pharmacist pharmacist = (Pharmacist) getIntent().getSerializableExtra("pharmacist");

        binding.btnMenu.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            popup.getMenu().add("Remove Pharmacist");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Remove Pharmacist")) {
                    showRemoveConfirmation(pharmacist);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        if (pharmacist != null) {
            binding.tvPharmacistName.setText(pharmacist.getName());
            binding.tvStatusBadge.setText(pharmacist.getStatus().toUpperCase());
            binding.tvStaffId.setText(pharmacist.getId());
            binding.tvExperience.setText(pharmacist.getExperience());
            binding.tvMobileNumber.setText(pharmacist.getMobile());
            binding.tvEmail.setText(pharmacist.getEmail());

            if (pharmacist.getStatus().equalsIgnoreCase("ACTIVE")) {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);
                binding.tvStatusBadge.setTextColor(Color.parseColor("#00897B"));
                binding.statusIndicator.setBackgroundResource(R.drawable.bg_circle_green);
            } else {
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_grey);
                binding.tvStatusBadge.setTextColor(Color.parseColor("#5A6B82"));
                binding.statusIndicator.setBackgroundResource(R.drawable.bg_circle_grey);
            }
        }
    }

    private void showRemoveConfirmation(Pharmacist pharmacist) {
        String name = (pharmacist != null ? pharmacist.getName() : "this pharmacist");
        com.simats.frontend.utils.DialogHelper.showConfirmationDialog(
                this,
                R.drawable.ic_logout, // Generic warning icon
                "Remove Pharmacist",
                "Are you sure you want to remove " + name + " from the system?",
                "Remove",
                "Cancel",
                () -> removePharmacist(pharmacist)
        );
    }

    private void removePharmacist(Pharmacist pharmacist) {
        android.content.SharedPreferences prefs = getSharedPreferences("AdminStats", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        
        // Decrement count
        int count = prefs.getInt("pharm_count", 15);
        editor.putInt("pharm_count", Math.max(0, count - 1));
        
        // Remove from dynamic list if present
        if (pharmacist != null) {
            String newPharms = prefs.getString("new_pharmacists_data", "");
            if (!newPharms.isEmpty()) {
                String[] pharms = newPharms.split(";");
                StringBuilder updatedPharms = new StringBuilder();
                for (String p : pharms) {
                    if (p.contains(pharmacist.getName()) && p.contains(pharmacist.getMobile())) {
                        continue; // Skip this one
                    }
                    if (updatedPharms.length() > 0) updatedPharms.append(";");
                    updatedPharms.append(p);
                }
                editor.putString("new_pharmacists_data", updatedPharms.toString());
            }
        }
        
        editor.apply();
        android.widget.Toast.makeText(this, "User removed successfully", android.widget.Toast.LENGTH_SHORT).show();
        finish();
    }
}
