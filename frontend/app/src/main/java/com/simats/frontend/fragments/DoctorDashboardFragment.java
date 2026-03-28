package com.simats.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Toast;
import com.simats.frontend.databinding.FragmentDoctorDashboardBinding;

public class DoctorDashboardFragment extends Fragment {

        private FragmentDoctorDashboardBinding binding;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                        @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorDashboardBinding.inflate(inflater, container, false);

        com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(requireContext());
        String doctorName = sessionManager.getName();
        if (doctorName != null && !doctorName.isEmpty()) {
                String cleanName = doctorName;
                if (cleanName.toLowerCase().startsWith("dr. ")) {
                        cleanName = cleanName.substring(4).trim();
                } else if (cleanName.toLowerCase().startsWith("dr.")) {
                        cleanName = cleanName.substring(3).trim();
                } else if (cleanName.toLowerCase().startsWith("dr ")) {
                        cleanName = cleanName.substring(3).trim();
                }
                binding.tvDoctorName.setText("Dr. " + cleanName);
        }
 
        setupRecentActivity();
        setupClickListeners();
 
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchRecentActivity();
    }

    private void setupRecentActivity() {
        binding.rvRecentActivity.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        fetchRecentActivity();
    }

    private void fetchRecentActivity() {
        if (getContext() == null || binding == null) return;

        com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(requireContext());
        String doctorId = sessionManager.getUserId();
        if (doctorId == null || doctorId.isEmpty()) return;

        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient
                .getClient(requireContext()).create(com.simats.frontend.network.ApiInterface.class);

        apiInterface.getPrescriptionsByDoctor(doctorId).enqueue(new retrofit2.Callback<java.util.List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call,
                                   retrofit2.Response<java.util.List<com.google.gson.JsonObject>> response) {
                if (getActivity() == null || binding == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<com.simats.frontend.models.DashboardActivityItem> items = new java.util.ArrayList<>();
                    java.util.List<com.google.gson.JsonObject> fullList = response.body();

                    // Calculate precise totals for Pending and Dispensed
                    int pendingCount = 0;
                    int dispensedCount = 0;
                    for (com.google.gson.JsonObject presc : fullList) {
                        String status = presc.has("status") && !presc.get("status").isJsonNull() ? presc.get("status").getAsString() : "";
                        // Count as Pending if status is CREATED, PENDING, ISSUED or empty/null
                        if (status == null || status.isEmpty() || "ISSUED".equalsIgnoreCase(status) || 
                            "CREATED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) {
                            pendingCount++;
                        } else if ("DISPENSED".equalsIgnoreCase(status)) {
                            dispensedCount++;
                        }
                    }
                    if (binding.tvPendingCount != null) binding.tvPendingCount.setText(String.valueOf(pendingCount));
                    if (binding.tvDispensedCount != null) binding.tvDispensedCount.setText(String.valueOf(dispensedCount));

                    // Sort by createdAt descending to ensure newest records are first
                    java.util.Collections.sort(fullList, (a, b) ->{
                        String dateA = a.has("created_at") && !a.get("created_at").isJsonNull() ? a.get("created_at").getAsString() : "";
                        String dateB = b.has("created_at") && !b.get("created_at").isJsonNull() ? b.get("created_at").getAsString() : "";
                        return dateB.compareTo(dateA);
                    });

                    // DEDUPLICATE BY PATIENT: keep only the latest prescription per patient
                    // fullList is already sorted descending, so the first occurrence is the latest
                    java.util.LinkedHashMap<String, com.google.gson.JsonObject> patientMap = new java.util.LinkedHashMap<>();
                    for (com.google.gson.JsonObject presc : fullList) {
                        String pid = presc.has("patient_id") && !presc.get("patient_id").isJsonNull()
                                ? presc.get("patient_id").getAsString() : "0";
                        if (!patientMap.containsKey(pid)) {
                            patientMap.put(pid, presc);
                        }
                    }

                    // SHOW LATEST 3 UNIQUE PATIENTS
                    int count = 0;
                    for (com.google.gson.JsonObject presc : patientMap.values()) {
                        if (count >= 3) break;

                        String patientName = "Patient";
                        if (presc.has("patient_name") && !presc.get("patient_name").isJsonNull()) {
                            patientName = presc.get("patient_name").getAsString();
                        }

                        String createdAt = presc.has("created_at") && !presc.get("created_at").isJsonNull() ? presc.get("created_at").getAsString() : "";
                        String status = presc.has("status") && !presc.get("status").isJsonNull() ? presc.get("status").getAsString() : "Pending";
                        String rxId = "RX-" + (presc.has("id") && !presc.get("id").isJsonNull() ? presc.get("id").getAsString() : "0000");

                        // SMART DATE FORMATTING: "Today, 10:45 AM" / "Yesterday, 09:30 PM"
                        String dateLabel = formatDateLabel(createdAt);
                        String subtitle = rxId + " • " + dateLabel;

                        String initials = "PA";
                        if (patientName != null && !patientName.isEmpty()) {
                            String[] parts = patientName.trim().split("\\s+");
                            if (parts.length >= 2) {
                                initials = (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase();
                            } else {
                                initials = patientName.substring(0, Math.min(2, patientName.length())).toUpperCase();
                            }
                        }

                        String uiStatus = (status == null || status.isEmpty() || "ISSUED".equalsIgnoreCase(status) ||
                                           "CREATED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) ? "Pending" :
                                          "DISPENSED".equalsIgnoreCase(status) ? "Dispensed" : status;

                        items.add(new com.simats.frontend.models.DashboardActivityItem(patientName, subtitle, uiStatus, initials));
                        count++;
                    }
                    
                    if (items.isEmpty()) {
                        binding.rvRecentActivity.setAdapter(null);
                        return;
                    }

                    com.simats.frontend.adapters.DashboardActivityAdapter adapter = new com.simats.frontend.adapters.DashboardActivityAdapter(
                            getContext(), items);
                    binding.rvRecentActivity.setAdapter(adapter);
                } else {
                    binding.rvRecentActivity.setAdapter(null);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, Throwable t) {
                android.util.Log.e("DoctorDashboard", "Refresh error: " + t.getMessage());
            }
        });
    }

    private String formatDateLabel(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "Recently";
        try {
            // Normalize ISO date: replace ' ' with 'T' if needed and remove trailing 'Z' if present
            String cleanIso = isoDate.replace(" ", "T");
            
            // Expected format from MySQL: "2023-10-24T10:30:00.000Z" or "2023-10-24 10:30:00"
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            
            java.util.Date date = inputFormat.parse(cleanIso);
            
            // Output format in IST
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
            android.util.Log.e("DoctorDashboard", "Date parse error: " + e.getMessage());
            return isoDate;
        }
    }


    private void setupClickListeners() {
        binding.ivDoctorAvatar.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireActivity(),
                            com.simats.frontend.DoctorSettingsActivity.class);
            startActivity(intent);
        });

        binding.tvViewAll.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireActivity(),
                             com.simats.frontend.PrescriptionHistoryActivity.class);
            intent.putExtra("filter_status", "ALL");
            startActivity(intent);
        });

        binding.cardPending.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireActivity(),
                    com.simats.frontend.PrescriptionHistoryActivity.class);
            intent.putExtra("filter_status", "Pending");
            startActivity(intent);
        });

        binding.cardDispensed.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireActivity(),
                    com.simats.frontend.PrescriptionHistoryActivity.class);
            intent.putExtra("filter_status", "Dispensed");
            startActivity(intent);
        });
    }

        @Override
        public void onDestroyView() {
                super.onDestroyView();
                binding = null;
        }
}
