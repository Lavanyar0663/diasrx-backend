package com.simats.frontend;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simats.frontend.adapters.RequestAdapter;
import com.simats.frontend.databinding.ActivityAccessRequestsBinding;
import com.simats.frontend.models.AccessRequest;

import java.util.ArrayList;
import java.util.List;

public class AccessRequestsActivity extends AppCompatActivity {

    private ActivityAccessRequestsBinding binding;
    private RequestAdapter adapter;
    private List<AccessRequest> allRequests;
    private List<AccessRequest> filteredRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccessRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        
        allRequests = new ArrayList<>();
        filteredRequests = new ArrayList<>();
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(this));

        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);

        adapter = new RequestAdapter(this, filteredRequests, new RequestAdapter.OnActionListener() {
            @Override
            public void onApprove(AccessRequest request) {
                apiInterface.approveRequest(request.getId()).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AccessRequestsActivity.this, "Request Approved", Toast.LENGTH_SHORT).show();
                            fetchRequests(apiInterface);
                        } else {
                            Toast.makeText(AccessRequestsActivity.this, "Failed to approve", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                        Toast.makeText(AccessRequestsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject(AccessRequest request) {
                apiInterface.rejectRequest(request.getId()).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AccessRequestsActivity.this, "Request Rejected", Toast.LENGTH_SHORT).show();
                            fetchRequests(apiInterface);
                        } else {
                            Toast.makeText(AccessRequestsActivity.this, "Failed to reject", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                        Toast.makeText(AccessRequestsActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReconsider(AccessRequest request) {
                Toast.makeText(AccessRequestsActivity.this, "Action not natively supported purely logged via backend", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(AccessRequest request) {
                Intent intent = new Intent(AccessRequestsActivity.this, RequestProfileActivity.class);
                intent.putExtra("request_data", request);
                startActivity(intent);
            }
        });

        binding.rvRequests.setAdapter(adapter);

        // Setup Tabs
        binding.tabPending.setOnClickListener(v -> filterList("PENDING"));
        binding.tabApproved.setOnClickListener(v -> filterList("APPROVED"));
        binding.tabRejected.setOnClickListener(v -> filterList("REJECTED"));

        // Fetch initial data
        fetchRequests(apiInterface);
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);
        fetchRequests(apiInterface);
    }

    private void fetchRequests(com.simats.frontend.network.ApiInterface apiInterface) {
        apiInterface.getAllRequests().enqueue(new retrofit2.Callback<java.util.List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, retrofit2.Response<java.util.List<com.google.gson.JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allRequests.clear();
                    int size = response.body().size();
                    for (int i = 0; i < size; i++) {
                        com.google.gson.JsonObject pJson = response.body().get(i);
                        String idStr = pJson.has("id") && !pJson.get("id").isJsonNull() ? pJson.get("id").getAsString() : "";
                        String name = pJson.has("name") && !pJson.get("name").isJsonNull() ? pJson.get("name").getAsString() : "New User";
                        String role = pJson.has("role") && !pJson.get("role").isJsonNull() ? pJson.get("role").getAsString() : "Staff";
                        if (role != null && role.length() > 0) {
                            role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                        }
                        String email = pJson.has("email") && !pJson.get("email").isJsonNull() ? pJson.get("email").getAsString() : "";
                        String phone = pJson.has("phone") && !pJson.get("phone").isJsonNull() ? pJson.get("phone").getAsString() : "";
                        String dept = pJson.has("department") && !pJson.get("department").isJsonNull() ? pJson.get("department").getAsString() : "General";
                        String status = pJson.has("status") && !pJson.get("status").isJsonNull() ? pJson.get("status").getAsString() : "PENDING";
                        
                        String formattedDate = pJson.has("formatted_date") && !pJson.get("formatted_date").isJsonNull() ? pJson.get("formatted_date").getAsString() : "Just Now";

                        allRequests.add(new AccessRequest(
                                idStr, name, role, dept, email, phone, formattedDate, status, R.drawable.ic_person));
                    }
                    updateCounts();
                    filterList(currentFilter);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, Throwable t) {
                Toast.makeText(AccessRequestsActivity.this, "Failed to fetch requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String currentFilter = "PENDING";

    private void updateCounts() {
        int pending = 0, approved = 0, rejected = 0;
        for (AccessRequest req : allRequests) {
            String s = req.getStatus().toUpperCase();
            if (s.equals("PENDING")) pending++;
            else if (s.equals("APPROVED")) approved++;
            else if (s.equals("REJECTED")) rejected++;
        }

        int pendingBadgeCount = Math.max(0, pending - 3);
        binding.tvCountPending.setText(String.valueOf(pendingBadgeCount));
        binding.tvCountApproved.setText(String.valueOf(approved));
        binding.tvCountRejected.setText(String.valueOf(rejected));

        binding.tvCountPending.setVisibility(pendingBadgeCount > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.tvCountApproved.setVisibility(approved > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.tvCountRejected.setVisibility(rejected > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void filterList(String status) {
        currentFilter = status;
        
        // Reset tabs UI
        binding.tabPending.setBackground(null);
        binding.tabApproved.setBackground(null);
        binding.tabRejected.setBackground(null);
        
        // Labels are children of the LinearLayout tabs
        ((android.widget.TextView) binding.tabPending.getChildAt(0)).setTextColor(Color.GRAY);
        ((android.widget.TextView) binding.tabApproved.getChildAt(0)).setTextColor(Color.GRAY);
        ((android.widget.TextView) binding.tabRejected.getChildAt(0)).setTextColor(Color.GRAY);

        if (status.equals("PENDING")) {
            binding.tabPending.setBackgroundResource(R.drawable.bg_badge_white_shadow);
            ((android.widget.TextView) binding.tabPending.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.colorSecondary));
            ((android.widget.TextView) binding.tabPending.getChildAt(0)).setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (status.equals("APPROVED")) {
            binding.tabApproved.setBackgroundResource(R.drawable.bg_badge_white_shadow);
            ((android.widget.TextView) binding.tabApproved.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.colorSecondary));
            ((android.widget.TextView) binding.tabApproved.getChildAt(0)).setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            binding.tabRejected.setBackgroundResource(R.drawable.bg_badge_white_shadow);
            ((android.widget.TextView) binding.tabRejected.getChildAt(0)).setTextColor(ContextCompat.getColor(this, R.color.colorSecondary));
            ((android.widget.TextView) binding.tabRejected.getChildAt(0)).setTypeface(null, android.graphics.Typeface.BOLD);
        }

        filteredRequests.clear();
        int pendingSeen = 0;
        for (AccessRequest req : allRequests) {
            if (req.getStatus().equalsIgnoreCase(status)) {
                if (status.equalsIgnoreCase("PENDING")) {
                    pendingSeen++;
                    if (pendingSeen > 3) {
                        filteredRequests.add(req);
                    }
                } else {
                    filteredRequests.add(req);
                }
            }
        }
        
        if (filteredRequests.isEmpty()) {
            binding.rvRequests.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            if (status.equalsIgnoreCase("PENDING") && pendingSeen > 0 && pendingSeen <= 3) {
                binding.tvEmptyState.setText("All recent requests are shown on the dashboard.");
            } else {
                binding.tvEmptyState.setText("No " + status.toLowerCase() + " requests found.");
            }
        } else {
            binding.rvRequests.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setVisibility(View.GONE);
        }
        
        adapter.notifyDataSetChanged();
    }

    private void showRejectionReason(AccessRequest request) {
        Intent intent = new Intent(this, RejectionReasonActivity.class);
        intent.putExtra("applicant_name", request.getName());
        intent.putExtra("role", request.getRole());
        startActivity(intent);
    }
}
