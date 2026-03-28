package com.simats.frontend;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityNotificationSettingsBinding;

public class NotificationSettingsActivity extends AppCompatActivity {

    private ActivityNotificationSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button navigation — returns to DoctorSettingsActivity
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // Handle Save Changes
        binding.btnSaveChanges.setOnClickListener(v -> {
            savePreferences();
            showSuccessPopup("Notification Settings Saved");

            // Auto-navigate back after popup
            new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1600);
        });
    }

    private void savePreferences() {
        // In a real app: save to SharedPreferences or API
        // Current state is reflected by the switch checked states
    }

    private void showSuccessPopup(String message) {
        try {
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.layout_success_popup);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(0.1f);
                
                // Position popup at the bottom like a Snackbar
                dialog.getWindow().setGravity(android.view.Gravity.BOTTOM);
                android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                params.y = 100; // Offset from bottom edge
                dialog.getWindow().setAttributes(params);
            }

            TextView tv = dialog.findViewById(R.id.tvPopupMessage);
            if (tv != null) tv.setText(message);

            dialog.setCancelable(true);
            dialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (dialog.isShowing()) dialog.dismiss();
            }, 1400);
        } catch (Exception e) {
            // Silently ignore dialog errors
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
