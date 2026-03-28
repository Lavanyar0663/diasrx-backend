package com.simats.frontend;

import android.graphics.Color;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simats.frontend.adapters.PrescriptionHistoryAdapter;
import com.simats.frontend.databinding.ActivityPrescriptionHistoryBinding;
import com.simats.frontend.models.PrescriptionHistoryItem;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionHistoryActivity extends AppCompatActivity {

    private ActivityPrescriptionHistoryBinding binding;
    private List<PrescriptionHistoryItem> allItems = new ArrayList<>();        // all prescriptions (raw, per RX)
    private List<PrescriptionHistoryItem> uniquePatientItems = new ArrayList<>(); // deduplicated per patient (for All tab)
    private String activeFilter = "ALL"; // Default to ALL tab as requested
    private com.simats.frontend.network.ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityPrescriptionHistoryBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);

            binding.ivBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.cvProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, PharmacistProfileActivity.class));
        });

            setupFilters();
            setupSearch();

            // Set up RecyclerView with empty list initially (no flicker)
            binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
            binding.rvHistory.setAdapter(new PrescriptionHistoryAdapter(this, new ArrayList<>()));

            String statusFilter = getIntent().getStringExtra("filter_status");
            if (statusFilter != null) {
                if ("Issued".equalsIgnoreCase(statusFilter) || "Pending".equalsIgnoreCase(statusFilter)) {
                    activeFilter = "Pending";
                } else if ("Dispensed".equalsIgnoreCase(statusFilter)) {
                    activeFilter = "Dispensed";
                } else if ("ALL".equalsIgnoreCase(statusFilter)) {
                    activeFilter = "ALL";
                }
            }

            // fetchHistory() will be called in onResume for the initial load
        } catch (Throwable t) {
            t.printStackTrace();
            android.util.Log.e("PrescriptionHistory", "Critical Error in onCreate: ", t);
            android.widget.Toast.makeText(this, "Error loading history: " + t.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            finish(); 
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchHistory();
    }

    private void fetchHistory() {
        com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(this);
        String role = sessionManager.getRole();
        String userId = sessionManager.getUserId();

        retrofit2.Call<List<com.google.gson.JsonObject>> call;
        if ("doctor".equalsIgnoreCase(role)) {
            call = apiInterface.getPrescriptionsByDoctor(userId);
        } else {
            call = apiInterface.getPrescriptionHistory();
        }

        call.enqueue(new retrofit2.Callback<List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.google.gson.JsonObject>> call,
                    retrofit2.Response<List<com.google.gson.JsonObject>> response) {
                allItems.clear();
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<com.google.gson.JsonObject> fullList = response.body();

                    // Sort by createdAt descending to ensure newest records are first
                    java.util.Collections.sort(fullList, (a, b) -> {
                        String dateA = a.has("created_at") && !a.get("created_at").isJsonNull() ? a.get("created_at").getAsString() : "";
                        String dateB = b.has("created_at") && !b.get("created_at").isJsonNull() ? b.get("created_at").getAsString() : "";
                        try {
                            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
                            f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                            java.util.Date tA = f.parse(dateA.length() > 19 ? dateA.substring(0, 19).replace(" ", "T") : dateA.replace(" ", "T"));
                            java.util.Date tB = f.parse(dateB.length() > 19 ? dateB.substring(0, 19).replace(" ", "T") : dateB.replace(" ", "T"));
                            return tB.compareTo(tA);
                        } catch (Exception e) {
                            return dateB.compareTo(dateA);
                        }
                    });

                    uniquePatientItems.clear();


                    // IDENTIFY RECENT ACTIVITY (TOP 3) TO EXCLUDE FROM VIEW ALL
                    java.util.Set<String> recentPrescriptionIds = new java.util.HashSet<>();
                    java.util.LinkedHashMap<String, com.google.gson.JsonObject> recentMap = new java.util.LinkedHashMap<>();
                    for (com.google.gson.JsonObject p : fullList) {
                        String pid = p.has("patient_id") && !p.get("patient_id").isJsonNull() ? p.get("patient_id").getAsString() : "0";
                        if (!recentMap.containsKey(pid)) recentMap.put(pid, p);
                    }
                    int rCount = 0;
                    for (com.google.gson.JsonObject p : recentMap.values()) {
                        if (rCount >= 3) break;
                        String rPrescId = p.has("id") && !p.get("id").isJsonNull() ? p.get("id").getAsString() : "0";
                        recentPrescriptionIds.add(rPrescId);
                        rCount++;
                    }

                    // Build per-prescription list (used by Pending / Dispensed tabs)
                    java.util.Set<String> seenPrescIds = new java.util.LinkedHashSet<>();
                    for (com.google.gson.JsonObject p : fullList) {
                        String prescId = p.has("id") && !p.get("id").isJsonNull() ? p.get("id").getAsString() : "0";

                        if (!seenPrescIds.add(prescId)) continue;

                        String patientId = p.has("patient_id") && !p.get("patient_id").isJsonNull() ? p.get("patient_id").getAsString() : "0";
                        String name = p.has("patient_name") && !p.get("patient_name").isJsonNull() ? p.get("patient_name").getAsString() : "Unknown";
                        String[] parts2 = name.trim().split("\\s+");
                        String initials2 = parts2.length >= 2
                                ? String.valueOf(parts2[0].charAt(0)).toUpperCase() + String.valueOf(parts2[1].charAt(0)).toUpperCase()
                                : name.substring(0, Math.min(2, name.length())).toUpperCase();
                        String opdId2 = "RX-" + prescId;
                        String doctorRaw2 = p.has("doctor_name") ? p.get("doctor_name").getAsString() : "Unknown";
                        String doctor2 = doctorRaw2.startsWith("Dr.") ? doctorRaw2 : "Dr. " + doctorRaw2;
                        String rawStatus2 = p.has("status") ? p.get("status").getAsString() : "";
                        String status2 = "DISPENSED".equalsIgnoreCase(rawStatus2) ? "Dispensed" : "Pending";
                        String createdAt2 = p.has("created_at") ? p.get("created_at").getAsString() : "";
                        String email2 = p.has("patient_email") ? p.get("patient_email").getAsString() : null;
                        allItems.add(new com.simats.frontend.models.PrescriptionHistoryItem(
                            name, initials2, opdId2, "9123456789",
                            doctor2, "Medicine A - Twice Daily", formatDateLabel(createdAt2), status2, email2, patientId));
                    }

                    // Build per-patient deduplicated list (used by All tab)
                    java.util.LinkedHashMap<String, com.google.gson.JsonObject> patientMap = new java.util.LinkedHashMap<>();
                    for (com.google.gson.JsonObject p : fullList) {
                        String prescId = p.has("id") && !p.get("id").isJsonNull() ? p.get("id").getAsString() : "0";
                        if (recentPrescriptionIds.contains(prescId)) continue; // SKIP Recent Activity

                        String pid = p.has("patient_id") && !p.get("patient_id").isJsonNull()
                                ? p.get("patient_id").getAsString() : "0";
                        if (!patientMap.containsKey(pid)) patientMap.put(pid, p);
                    }
                    for (com.google.gson.JsonObject p : patientMap.values()) {
                        String patientId = p.has("patient_id") && !p.get("patient_id").isJsonNull() ? p.get("patient_id").getAsString() : "0";
                        String name = p.has("patient_name") && !p.get("patient_name").isJsonNull() ? p.get("patient_name").getAsString() : "Unknown";

                        // Build 2-letter initials
                        String[] parts = name.trim().split("\\s+");
                        String initials;
                        if (parts.length >= 2) {
                            initials = String.valueOf(parts[0].charAt(0)).toUpperCase()
                                     + String.valueOf(parts[1].charAt(0)).toUpperCase();
                        } else {
                            initials = name.substring(0, Math.min(2, name.length())).toUpperCase();
                        }

                        String opdId = "RX-" + (p.has("id") ? p.get("id").getAsString() : "0000");
                        String doctorRaw = p.has("doctor_name") ? p.get("doctor_name").getAsString() : "Unknown";
                        String doctor = doctorRaw.startsWith("Dr.") ? doctorRaw : "Dr. " + doctorRaw;

                        // Normalize to consistent values: "Pending" or "Dispensed"
                        String rawStatus = p.has("status") ? p.get("status").getAsString() : "";
                        String status = "DISPENSED".equalsIgnoreCase(rawStatus) ? "Dispensed" : "Pending";

                        String createdAt = p.has("created_at") ? p.get("created_at").getAsString() : "";
                        String dateLabel = formatDateLabel(createdAt);

                        String email = p.has("patient_email") ? p.get("patient_email").getAsString() : null;

                        uniquePatientItems.add(new com.simats.frontend.models.PrescriptionHistoryItem(
                            name, initials, opdId, "9123456789",
                            doctor, "Medicine A - Twice Daily", dateLabel, status, email, patientId));
                    }
                } else {
                    allItems.clear();
                    uniquePatientItems.clear();
                    android.widget.Toast.makeText(PrescriptionHistoryActivity.this, "No prescription records found.", android.widget.Toast.LENGTH_SHORT).show();
                }
                applyFilter(activeFilter);
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.google.gson.JsonObject>> call, Throwable t) {
                android.util.Log.e("PrescriptionHistory", "API Failure: " + t.getMessage());
                allItems.clear();
                applyFilter(activeFilter);
                android.widget.Toast.makeText(PrescriptionHistoryActivity.this, "Network error: Unable to load history.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String formatDateLabel(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "Recently";
        try {
            String cleanIso = isoDate.replace(" ", "T");
            if (cleanIso.length() > 19) cleanIso = cleanIso.substring(0, 19);
            
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date date = inputFormat.parse(cleanIso);
            
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm aa", java.util.Locale.US);
            timeFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
            String timePart = timeFormat.format(date);
            
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
            String datePart = dateFormat.format(date);
            
            java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
            String todayStr = dateFormat.format(cal.getTime());
            
            cal.add(java.util.Calendar.DATE, -1);
            String yesterdayStr = dateFormat.format(cal.getTime());

            if (datePart.equals(todayStr)) return "Today, " + timePart;
            if (datePart.equals(yesterdayStr)) return "Yesterday, " + timePart;
            return datePart + ", " + timePart;
        } catch (Exception e) {
            return isoDate.contains("T") ? isoDate.replace("T", " ").substring(0, 16) : isoDate;
        }
    }


    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterBySearch(String query) {
        // All tab → deduplicated per patient; Pending/Dispensed → per prescription (all matching RXs)
        List<PrescriptionHistoryItem> sourceList = "ALL".equalsIgnoreCase(activeFilter)
                ? uniquePatientItems : allItems;

        List<PrescriptionHistoryItem> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();
        for (PrescriptionHistoryItem item : sourceList) {
            boolean matchesSearch = lowerQuery.isEmpty()
                    || item.getName().toLowerCase().contains(lowerQuery)
                    || item.getOpdId().toLowerCase().contains(lowerQuery);
            boolean matchesFilter = "ALL".equalsIgnoreCase(activeFilter)
                    || item.getStatus().equalsIgnoreCase(activeFilter);

            if (matchesSearch && matchesFilter) {
                filtered.add(item);
            }
        }
        updateRecyclerView(filtered);
    }

    private void setupFilters() {
        binding.tvFilterAll.setOnClickListener(v -> applyFilter("ALL"));
        binding.tvFilterPending.setOnClickListener(v -> applyFilter("Pending"));
        binding.tvFilterDispensed.setOnClickListener(v -> applyFilter("Dispensed"));
    }

    private void applyFilter(String filter) {
        activeFilter = filter;

        // Header and subtitle are now static as per user request
        // binding.tvHeaderTitle.setText(titleKey + " Prescriptions");

        resetPill(binding.tvFilterAll);
        resetPill(binding.tvFilterPending);
        resetPill(binding.tvFilterDispensed);

        android.widget.TextView selectedPill;
        if ("Pending".equalsIgnoreCase(filter)) {
            selectedPill = binding.tvFilterPending;
        } else if ("Dispensed".equalsIgnoreCase(filter)) {
            selectedPill = binding.tvFilterDispensed;
        } else {
            selectedPill = binding.tvFilterAll;
        }

        // Highlight the selected tab
        selectedPill.setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_pill_filter_selected));
        selectedPill.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary));
        selectedPill.setTypeface(null, Typeface.BOLD);

        filterBySearch(binding.etSearch.getText().toString());
    }

    private void updateRecyclerView(List<PrescriptionHistoryItem> filtered) {
        if (binding.rvHistory.getAdapter() == null) {
            binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        }
        PrescriptionHistoryAdapter adapter = new PrescriptionHistoryAdapter(this, filtered);
        binding.rvHistory.setAdapter(adapter);
    }

    private void resetPill(android.widget.TextView pill) {
        pill.setBackground(null);
        pill.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.colorTextSecondary));
        pill.setTypeface(null, Typeface.NORMAL);
    }
}
