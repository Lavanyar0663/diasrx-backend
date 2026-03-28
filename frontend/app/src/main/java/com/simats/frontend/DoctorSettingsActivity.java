package com.simats.frontend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityDoctorSettingsBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;
import com.simats.frontend.network.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorSettingsActivity extends AppCompatActivity {

    private ActivityDoctorSettingsBinding binding;
    private SessionManager sessionManager;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            binding = ActivityDoctorSettingsBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
        } catch (Exception e) {
            Toast.makeText(this, "Error loading settings", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionManager = new SessionManager(this);

        try {
            apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        } catch (Exception e) {
            // App can still work with cached session data
            apiInterface = null;
        }

        // Back button
        binding.ivBack.setOnClickListener(v -> finish());

        // Populate from cached session first
        loadProfileData();

        // Then try to refresh from API in background
        if (apiInterface != null) {
            fetchProfileFromApi();
        }

        // Options
        binding.btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        binding.btnNotifications.setOnClickListener(v -> {
            startActivity(new Intent(this, NotificationSettingsActivity.class));
        });

        binding.btnPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        binding.btnAbout.setOnClickListener(v ->
                Toast.makeText(this, "DIAS Rx v1.0", Toast.LENGTH_SHORT).show());

        // Logout
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadProfileData() {
        try {
            String name = sessionManager.getName();
            String email = sessionManager.getEmail();
            String spec = sessionManager.getSpec();
            String avatar = sessionManager.getAvatar();

            binding.tvDoctorName.setText(name != null && !name.isEmpty() ? name : "Doctor");
            binding.tvDesignation.setText(email != null ? email : "");
            if (spec != null && !spec.isEmpty()) {
                binding.tvDepartment.setText(spec);
            }

            if (avatar != null && !avatar.isEmpty()) {
                try {
                    binding.ivDoctorAvatar.setImageURI(Uri.parse(avatar));
                    binding.ivDoctorAvatar.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    binding.ivDoctorAvatar.setImageResource(R.drawable.ic_person);
                }
            }
        } catch (Exception e) {
            // Keep default values
        }
    }

    private void fetchProfileFromApi() {
        if (apiInterface == null) return;
        try {
            apiInterface.getDoctorProfile().enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JsonObject profile = response.body();
                            String name = profile.has("name") && !profile.get("name").isJsonNull()
                                    ? profile.get("name").getAsString() : sessionManager.getName();
                            String phone = profile.has("phone") && !profile.get("phone").isJsonNull()
                                    ? profile.get("phone").getAsString() : "";
                            String title = profile.has("professional_title") && !profile.get("professional_title").isJsonNull()
                                    ? profile.get("professional_title").getAsString() : "";
                            String spec = profile.has("specialization") && !profile.get("specialization").isJsonNull()
                                    ? profile.get("specialization").getAsString() : "";
                            String avatar = profile.has("avatar_url") && !profile.get("avatar_url").isJsonNull()
                                    ? profile.get("avatar_url").getAsString() : "";

                            sessionManager.updateProfile(name, phone, title, spec, avatar);
                            loadProfileData();
                        } catch (Exception e) {
                            // Silently keep cached data
                        }
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    // Keep local data — no crash
                }
            });
        } catch (Exception e) {
            // Silently fail
        }
    }

    private void showLogoutConfirmation() {
        try {
            com.simats.frontend.utils.DialogHelper.showLogoutDialog(this, () -> {
                sessionManager.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        } catch (Exception e) {
            // Fallback direct logout
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }
}
