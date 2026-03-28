package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simats.frontend.adapters.RequestAdapter;
import com.simats.frontend.databinding.ActivityAdminDashboardBinding;
import com.simats.frontend.models.AccessRequest;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private RequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup dynamic user data
        com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(this);
        String adminName = sessionManager.getUserName();
        binding.tvSubtitle.setText("Welcome back " + (adminName != null ? adminName : "Admin") + ", here is your summary.");

        // Setup RecyclerView for Recent Requests (Preview)
        setupRecentRequests();

        // Setup Card Clicks
        binding.cardManageDoctors.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageDoctorsActivity.class);
            startActivity(intent);
        });

        binding.cardManagePharm.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManagePharmacistsActivity.class);
            startActivity(intent);
        });

        binding.cardManagePatients.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManagePatientsActivity.class);
            startActivity(intent);
        });

        // View All Requests
        binding.tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(this, AccessRequestsActivity.class);
            startActivity(intent);
        });

        // Open Profile on Avatar/Profile icon click
        binding.ivAdminProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            startActivity(intent);
        });

        // Header icons handle navigation now

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            private long backPressedTime;
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finishAffinity();
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCounts();
        setupRecentRequests();
    }

    private void updateCounts() {
        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);
        apiInterface.getAdminStats().enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.google.gson.JsonObject stats = response.body();
                    int docCount = stats.has("doctors") && !stats.get("doctors").isJsonNull() ? stats.get("doctors").getAsInt() : 0;
                    int pharmCount = stats.has("pharmacists") && !stats.get("pharmacists").isJsonNull() ? stats.get("pharmacists").getAsInt() : 0;
                    int patientCount = stats.has("patients") && !stats.get("patients").isJsonNull() ? stats.get("patients").getAsInt() : 0;

                    binding.tvDoctorCount.setText(docCount + " Registered");
                    binding.tvPharmacistCount.setText(pharmCount + " Registered");
                    binding.tvPatientCountTotal.setText(String.format("%,d Registered", patientCount));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                // Silently fail or use last known
            }
        });
    }


    private void setupRecentRequests() {
        binding.rvRecentRequests.setLayoutManager(new LinearLayoutManager(this));

        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);
        retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call = apiInterface.getPendingRequests();

        call.enqueue(new retrofit2.Callback<java.util.List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, retrofit2.Response<java.util.List<com.google.gson.JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AccessRequest> list = new ArrayList<>();
                    int size = response.body().size();
                    int count = 0;
                    for (int i = 0; i < size; i++) {
                        if (count >= 3) break;
                        com.google.gson.JsonObject pJson = response.body().get(i);
                        String idStr = pJson.has("id") && !pJson.get("id").isJsonNull() ? pJson.get("id").getAsString() : "";
                        String name = pJson.has("name") && !pJson.get("name").isJsonNull() ? pJson.get("name").getAsString() : "New User";
                        String role = pJson.has("role") && !pJson.get("role").isJsonNull() ? pJson.get("role").getAsString() : "Staff";
                        String email = pJson.has("email") && !pJson.get("email").isJsonNull() ? pJson.get("email").getAsString() : "";
                        if (role != null && role.length() > 0) {
                            role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                        }
                        String phone = pJson.has("phone") && !pJson.get("phone").isJsonNull() ? pJson.get("phone").getAsString() : "";
                        String dept = pJson.has("department") && !pJson.get("department").isJsonNull() ? pJson.get("department").getAsString() : "";
                        String formattedDate = pJson.has("formatted_date") && !pJson.get("formatted_date").isJsonNull() ? pJson.get("formatted_date").getAsString() : "Just Now";
                        
                        list.add(new AccessRequest(
                                idStr, name, role, dept, email, phone, formattedDate, "PENDING", R.drawable.ic_person));
                        count++;
                    }

                    adapter = new RequestAdapter(AdminDashboardActivity.this, list, new RequestAdapter.OnActionListener() {
                        @Override
                        public void onApprove(AccessRequest request) {
                            apiInterface.approveRequest(request.getId()).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                                    if(response.isSuccessful()) {
                                        Toast.makeText(AdminDashboardActivity.this, "Approved " + request.getName(), Toast.LENGTH_SHORT).show();
                                        setupRecentRequests();
                                        updateCounts();
                                    } else {
                                        Toast.makeText(AdminDashboardActivity.this, "Failed to approve", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                                    Toast.makeText(AdminDashboardActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onReject(AccessRequest request) {
                            apiInterface.rejectRequest(request.getId()).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                                    if(response.isSuccessful()) {
                                        Toast.makeText(AdminDashboardActivity.this, "Rejected " + request.getName(), Toast.LENGTH_SHORT).show();
                                        setupRecentRequests();
                                        updateCounts();
                                    } else {
                                        Toast.makeText(AdminDashboardActivity.this, "Failed to reject", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                                    Toast.makeText(AdminDashboardActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onReconsider(AccessRequest request) {}

                        @Override
                        public void onItemClick(AccessRequest request) {
                            Intent intent = new Intent(AdminDashboardActivity.this, RequestProfileActivity.class);
                            intent.putExtra("request_data", request);
                            startActivity(intent);
                        }
                    });
                    binding.rvRecentRequests.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Failed to fetch requests", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
