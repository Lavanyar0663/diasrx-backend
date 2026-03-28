package com.simats.frontend.fragments;

import android.annotation.SuppressLint;
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
import com.simats.frontend.adapters.PrescriptionHistoryAdapter;
import com.simats.frontend.models.PrescriptionHistoryItem;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class PharmacistPatientsFragment extends Fragment {

    private RecyclerView rvHistory;
    private List<com.simats.frontend.models.DoctorPatient> patientItems = new ArrayList<>();
    private com.simats.frontend.adapters.PharmacistPatientAdapter adapter;
    private ApiInterface apiInterface;

    private android.widget.TextView tvTotalPatients;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pharmacist_patients, container, false);

        apiInterface = ApiClient.getClient(requireContext()).create(ApiInterface.class);

        rvHistory = view.findViewById(R.id.rvPatients);
        tvTotalPatients = view.findViewById(R.id.tvTotalPatients);
        
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new com.simats.frontend.adapters.PharmacistPatientAdapter(requireContext(), patientItems);
        rvHistory.setAdapter(adapter);

        android.widget.TextView tvHeaderTitle = view.findViewById(R.id.tvHeaderTitle);
        if (tvHeaderTitle == null) {
            // Check if ID was changed in a newer layout or keep as is
        }

        android.widget.EditText etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterHistory(s.toString());
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        setupProfileNavigation(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchHistory();
    }

    private void fetchHistory() {
        apiInterface.getPrescriptionHistory().enqueue(new Callback<List<JsonObject>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (!isAdded() || getContext() == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    java.util.Map<String, com.simats.frontend.models.DoctorPatient> patientMap = new java.util.LinkedHashMap<>();
                    
                    for (JsonObject p : response.body()) {
                        String name = p.has("patient_name") && !p.get("patient_name").isJsonNull() ? p.get("patient_name").getAsString() : "Unknown";
                        String patientId = p.has("patient_id") && !p.get("patient_id").isJsonNull() ? p.get("patient_id").getAsString() : "0";
                        String opdId = "#PX-" + patientId; // Matching Screen 3 format
                        
                        String createdAt = p.has("created_at") && !p.get("created_at").isJsonNull() ? p.get("created_at").getAsString() : "";
                        String dateLabel = formatDate(createdAt);
                        String timePart = createdAt.contains("T") ? createdAt.split("T")[1].substring(0, 5) : "Recently";
                        
                        // We use patientId as key to group. If multiple prescriptions, the newest one (first in list usually) wins for metadata.
                        if (!patientMap.containsKey(patientId)) {
                             // Extract age/gender if available, otherwise fallback
                             String age = p.has("patient_age") && !p.get("patient_age").isJsonNull() ? p.get("patient_age").getAsString() + " yrs" : "28 yrs";
                             String gender = p.has("patient_gender") && !p.get("patient_gender").isJsonNull() ? p.get("patient_gender").getAsString() : "Male";
                             
                             com.simats.frontend.models.DoctorPatient patient = new com.simats.frontend.models.DoctorPatient(
                                 name, opdId, age + " • " + gender, timePart, dateLabel);
                             // Set ID strictly for timeline navigation
                             try {
                                 java.lang.reflect.Field idField = patient.getClass().getDeclaredField("id");
                                 idField.setAccessible(true);
                                 idField.set(patient, patientId);
                             } catch (Exception e) {}
                             
                             patientMap.put(patientId, patient);
                        }
                    }
                    
                    patientItems.clear();
                    patientItems.addAll(patientMap.values());
                    
                    if (tvTotalPatients != null) {
                        tvTotalPatients.setText(patientItems.size() + " Total");
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {}
        });
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "Recently";
        try {
            String datePart = isoDate.contains("T") ? isoDate.split("T")[0] : isoDate;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(datePart);
            
            java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private void filterHistory(String query) {
        List<com.simats.frontend.models.DoctorPatient> filtered = new ArrayList<>();
        for (com.simats.frontend.models.DoctorPatient item : patientItems) {
            if (item.getName().toLowerCase().contains(query.toLowerCase()) || 
                item.getOpdId().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        adapter = new com.simats.frontend.adapters.PharmacistPatientAdapter(getContext(), filtered);
        rvHistory.setAdapter(adapter);
    }

    private void setupProfileNavigation(View view) {
        View cvProfile = view.findViewById(R.id.cvProfile);
        View ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        View.OnClickListener pClick = v -> startActivity(new android.content.Intent(getContext(), com.simats.frontend.PharmacistProfileActivity.class));
        if (cvProfile != null) cvProfile.setOnClickListener(pClick);
        if (ivProfileAvatar != null) ivProfileAvatar.setOnClickListener(pClick);
    }
}
