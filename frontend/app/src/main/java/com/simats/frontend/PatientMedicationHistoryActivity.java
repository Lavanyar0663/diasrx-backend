package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.adapters.MedicationHistoryAdapter;
import com.simats.frontend.databinding.ActivityPatientMedicationHistoryBinding;
import com.simats.frontend.models.DoctorPatient;
import com.simats.frontend.models.MedicationHistoryEntry;
import com.simats.frontend.models.MedicationHistoryEntry.DrugEntry;

import java.util.ArrayList;
import java.util.List;

public class PatientMedicationHistoryActivity extends AppCompatActivity {

    private ActivityPatientMedicationHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityPatientMedicationHistoryBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            binding.ivBack.setOnClickListener(v -> finish());

            // Receive patient data from intent
            DoctorPatient patient = (DoctorPatient) getIntent().getSerializableExtra("patient_data");

            if (patient != null) {
                binding.tvPatientName.setText(patient.getName());
                binding.tvAvatarInitials.setText(patient.getInitials());
                binding.tvOpdBadge.setText(patient.getOpdId());
                binding.tvAgeGender.setText(patient.getAgeGender());
                binding.tvPhone.setText(patient.getPhone());
            }

            // Fetch real medication history from API
            if (patient != null) {
                fetchMedicationHistory(String.valueOf(patient.getId()));
            }

            binding.rvMedicationHistory.setLayoutManager(new LinearLayoutManager(this));
        } catch (Throwable t) {
            t.printStackTrace();
            android.util.Log.e("PatientHistory", "Critical Error in onCreate: ", t);
            android.widget.Toast.makeText(this, "Error: " + t.getMessage(), android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void fetchMedicationHistory(String patientId) {
        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);
        apiInterface.getPatientPrescriptions(patientId).enqueue(new retrofit2.Callback<List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.google.gson.JsonObject>> call, retrofit2.Response<List<com.google.gson.JsonObject>> response) {
                if (isFinishing() || binding == null) return;
                
                if (response.isSuccessful() && response.body() != null) {
                    List<MedicationHistoryEntry> history = new ArrayList<>();
                    for (com.google.gson.JsonObject obj : response.body()) {
                        String formattedDateStr = obj.has("formatted_date") && !obj.get("formatted_date").isJsonNull() ? obj.get("formatted_date").getAsString() : "Just Now, --:--";
                        String[] dateParts = formattedDateStr.contains(", ") ? formattedDateStr.split(", ") : new String[]{"Just Now", "--:--"};
                        
                        String dept = obj.has("doctor_department") && !obj.get("doctor_department").isJsonNull() ? obj.get("doctor_department").getAsString() : "General Medicine";
                        String diagnosis = obj.has("diagnosis") && !obj.get("diagnosis").isJsonNull() ? obj.get("diagnosis").getAsString() : "Regular Checkup";
                        
                        List<DrugEntry> drugsList = new ArrayList<>();
                        if (obj.has("drugs") && obj.get("drugs").isJsonArray()) {
                            com.google.gson.JsonArray drugsArr = obj.getAsJsonArray("drugs");
                            for (int i = 0; i < drugsArr.size(); i++) {
                                com.google.gson.JsonObject drugObj = drugsArr.get(i).getAsJsonObject();
                                String name = drugObj.has("drug_name") && !drugObj.get("drug_name").isJsonNull() ? drugObj.get("drug_name").getAsString() : "Unknown Drug";
                                String type = drugObj.has("drug_type") && !drugObj.get("drug_type").isJsonNull() ? drugObj.get("drug_type").getAsString() : "";
                                String str = drugObj.has("strength") && !drugObj.get("strength").isJsonNull() ? drugObj.get("strength").getAsString() : "";
                                
                                String drugId = drugObj.has("drug_id") && !drugObj.get("drug_id").isJsonNull() ? drugObj.get("drug_id").getAsString() : "0";
                                String instructions = drugObj.has("instructions") && !drugObj.get("instructions").isJsonNull() ? drugObj.get("instructions").getAsString() : "Standard";
                                
                                String freq = drugObj.has("frequency") && !drugObj.get("frequency").isJsonNull() ? drugObj.get("frequency").getAsString() : "N/A";
                                String dur = drugObj.has("duration") && !drugObj.get("duration").isJsonNull() ? drugObj.get("duration").getAsString() : "N/A";
                                
                                drugsList.add(new DrugEntry(drugId, name, str + " " + type, freq, dur, instructions));
                            }
                        }
                        
                        history.add(new MedicationHistoryEntry(dateParts[0], dateParts[1], dept, diagnosis, drugsList));
                    }
                    
                    binding.rvMedicationHistory.setAdapter(new MedicationHistoryAdapter(PatientMedicationHistoryActivity.this, history, 
                        (DoctorPatient) getIntent().getSerializableExtra("patient_data")));
                } else {
                    android.widget.Toast.makeText(PatientMedicationHistoryActivity.this, "Failed to load history", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.google.gson.JsonObject>> call, Throwable t) {
                if (isFinishing()) return;
                android.widget.Toast.makeText(PatientMedicationHistoryActivity.this, "Network error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
