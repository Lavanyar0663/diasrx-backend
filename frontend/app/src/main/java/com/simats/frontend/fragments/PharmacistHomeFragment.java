package com.simats.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.adapters.PharmacistPrescriptionAdapter;
import com.simats.frontend.databinding.FragmentPharmacistHomeBinding;
import com.simats.frontend.models.PharmacistPrescription;

import java.util.ArrayList;
import java.util.List;

import com.simats.frontend.models.DashboardActivityItem;
import com.simats.frontend.adapters.DashboardActivityAdapter;

import android.content.Intent;
import com.simats.frontend.PrescriptionHistoryActivity;
import com.simats.frontend.PharmacistProfileActivity;
import com.simats.frontend.network.ApiInterface;
import com.simats.frontend.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.JsonObject;

public class PharmacistHomeFragment extends Fragment {

        private FragmentPharmacistHomeBinding binding;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                        @Nullable Bundle savedInstanceState) {
                binding = FragmentPharmacistHomeBinding.inflate(inflater, container, false);

                binding.rvLatestPrescriptions.setLayoutManager(new LinearLayoutManager(getContext()));

                // Set dynamic name using SessionManager
                com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(requireContext());
                String name = sessionManager.getUserName();
                if (name != null) {
                        name = name.replace("Ph. ", "").replace("Ph.", "").trim();
                }
                binding.tvWelcomeName.setText("Pharmacist " + (name != null ? name : "Admin"));

                binding.tvViewMore.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), com.simats.frontend.PrescriptionHistoryActivity.class);
                        intent.putExtra("filter_status", "ALL");
                        startActivity(intent);
                });

                // Pending card: open history filtered to Pending
                binding.cardPendingStatus.setOnClickListener(v -> {
                        Intent intent = new Intent(getContext(), com.simats.frontend.PrescriptionHistoryActivity.class);
                        intent.putExtra("filter_status", "Pending");
                        startActivity(intent);
                });

                binding.ivProfileAvatar.setOnClickListener(v -> {
                        startActivity(new Intent(getContext(), PharmacistProfileActivity.class));
                });

                binding.cvProfile.setOnClickListener(v -> {
                        startActivity(new Intent(getContext(), PharmacistProfileActivity.class));
                });

                fetchLatestPrescriptions();

                return binding.getRoot();
        }

        @Override
        public void onResume() {
                super.onResume();
                fetchLatestPrescriptions();
                fetchStats();
        }

        private void fetchStats() {
                if (getContext() == null || binding == null) return;

                ApiInterface apiInterface = ApiClient.getClient(requireContext()).create(ApiInterface.class);
                apiInterface.getPharmacistStats().enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (getActivity() == null || binding == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject stats = response.body();
                            int totalPending = stats.has("totalPending") ? stats.get("totalPending").getAsInt() : 0;
                            int dispensedToday = stats.has("dispensedToday") ? stats.get("dispensedToday").getAsInt() : 0;
                            int totalToday = stats.has("totalToday") ? stats.get("totalToday").getAsInt() : 0;
                            int totalDispensed = stats.has("totalDispensed") ? stats.get("totalDispensed").getAsInt() : 0;

                            // Update Pending Card
                            binding.tvTotalPending.setText(String.valueOf(totalPending));
                            
                            // Update Circular Progress (Overall Dispensed vs All)
                            int totalAll = totalPending + totalDispensed;
                            int progressAll = totalAll > 0 ? (totalDispensed * 100 / totalAll) : 0;
                            binding.pbCircular.setProgress(progressAll);
                            binding.tvCircularProgress.setText(progressAll + "%");

                            // Update Overall Dispensing Status
                            binding.tvProgressCount.setText(totalDispensed + " / " + totalAll);
                            int progressOverall = totalAll > 0 ? (totalDispensed * 100 / totalAll) : 0;
                            binding.pbDispensingProgress.setProgress(progressOverall);
                        } else {
                            String errorMsg = "Internal Server Error (" + response.code() + ")";
                            try {
                                if (response.errorBody() != null) {
                                    String errorJson = response.errorBody().string();
                                    com.google.gson.JsonObject errorObj = com.google.gson.JsonParser.parseString(errorJson).getAsJsonObject();
                                    if (errorObj.has("message")) errorMsg = errorObj.get("message").getAsString();
                                }
                            } catch (Exception e) { e.printStackTrace(); }
                            android.widget.Toast.makeText(getContext(), "Stats Error: " + errorMsg, android.widget.Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), "Connection failed: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        }
                        android.util.Log.e("PharmacistHome", "Stats connection failure", t);
                    }
                });
        }

        private void fetchLatestPrescriptions() {
                if (getContext() == null || binding == null) return;

                ApiInterface apiInterface = ApiClient.getClient(requireContext()).create(ApiInterface.class);
                apiInterface.getPrescriptionHistory().enqueue(new Callback<List<com.google.gson.JsonObject>>() {
                        @Override
                        public void onResponse(Call<List<com.google.gson.JsonObject>> call, Response<List<com.google.gson.JsonObject>> response) {
                                if (getActivity() == null || binding == null) return;
                                List<DashboardActivityItem> items = new ArrayList<>();

                                if (response.isSuccessful() && response.body() != null) {
                                        List<com.google.gson.JsonObject> allPrescs = response.body();
                                        int totalCount = allPrescs.size();

                                        // Sorting by created_at descending to ensure newest are first
                                        java.util.Collections.sort(allPrescs, (a, b) -> {
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

                                        // SHOW UP TO 3 LATEST PRESCRIPTIONS (Unique by patient)
                                        java.util.LinkedHashMap<String, com.google.gson.JsonObject> recentMap = new java.util.LinkedHashMap<>();
                                        for (com.google.gson.JsonObject presc : allPrescs) {
                                            String pid = presc.has("patient_id") && !presc.get("patient_id").isJsonNull() ? presc.get("patient_id").getAsString() : "0";
                                            // Since allPrescs is sorted nearest first, putting the first occurrence guarantees latest prescription per patient
                                            if (!recentMap.containsKey(pid)) recentMap.put(pid, presc);
                                        }

                                        int count = 0;
                                        for (com.google.gson.JsonObject presc : recentMap.values()) {
                                            if (count >= 3) break;

                                            String patientName = "Patient";
                                            if (presc.has("patient_name") && !presc.get("patient_name").isJsonNull()) {
                                                patientName = presc.get("patient_name").getAsString();
                                            }
                                            String rxId = "RX-" + (presc.has("id") && !presc.get("id").isJsonNull() ? presc.get("id").getAsString() : "0000");
                                            String createdAt = presc.has("created_at") && !presc.get("created_at").isJsonNull() ? presc.get("created_at").getAsString() : "";

                                            String dateLabel = formatDateLabel(createdAt);
                                            String subtitle = rxId + " \u2022 " + dateLabel;

                                            // Generate initials
                                            String initials = "P";
                                            if (patientName != null && !patientName.isEmpty()) {
                                                String[] nameParts = patientName.trim().split("\\s+");
                                                if (nameParts.length >= 2) {
                                                    initials = (nameParts[0].substring(0, 1) + nameParts[nameParts.length - 1].substring(0, 1)).toUpperCase();
                                                } else {
                                                    initials = patientName.substring(0, Math.min(2, patientName.length())).toUpperCase();
                                                }
                                            }

                                            String rawStatus = presc.has("status") && !presc.get("status").isJsonNull() ? presc.get("status").getAsString() : "";
                                            String status = "DISPENSED".equalsIgnoreCase(rawStatus) ? "Dispensed" : "Pending";

                                            items.add(new DashboardActivityItem(patientName, subtitle, status, initials));
                                            count++;
                                        }
                                } else {
                                    if (getContext() != null) {
                                        android.widget.Toast.makeText(getContext(), "Dashboard load failed: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                }
                                // Ensure adapter is always set even if empty to clear placeholders
                                com.simats.frontend.adapters.DashboardActivityAdapter adapter = new com.simats.frontend.adapters.DashboardActivityAdapter(getContext(), items);
                                binding.rvLatestPrescriptions.setAdapter(adapter);
                        }

                        @Override
                        public void onFailure(Call<List<com.google.gson.JsonObject>> call, Throwable t) {
                                android.util.Log.e("PharmacistHome", "Refresh error: " + t.getMessage());
                                // On failure, set an empty adapter
                                if (getActivity() != null && binding != null) {
                                    binding.rvLatestPrescriptions.setAdapter(new DashboardActivityAdapter(getContext(), new ArrayList<>()));
                                }
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

        @Override
        public void onDestroyView() {
                super.onDestroyView();
                binding = null;
        }

}
