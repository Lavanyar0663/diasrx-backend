package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simats.frontend.adapters.PatientAdapter;
import com.simats.frontend.databinding.ActivityManagePatientsBinding;
import com.simats.frontend.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class ManagePatientsActivity extends AppCompatActivity {

    private ActivityManagePatientsBinding binding;
    private PatientAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagePatientsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back action
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        binding.rvPatients.setLayoutManager(new LinearLayoutManager(this));

        // Search logic
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Fab action
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewPatientActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatients();
    }

    private void loadPatients() {
        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);
        apiInterface.getPatients().enqueue(new retrofit2.Callback<List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.google.gson.JsonObject>> call, retrofit2.Response<List<com.google.gson.JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Patient> patients = new ArrayList<>();
                    List<com.google.gson.JsonObject> body = response.body();
                    
                    // Sort by reg date or ID descending if needed, usually API does this
                    for (com.google.gson.JsonObject obj : body) {
                        String name = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "Unknown";
                        String pid = obj.has("pid") && !obj.get("pid").isJsonNull() ? obj.get("pid").getAsString() : "PID-0000";
                        String age = obj.has("age") && !obj.get("age").isJsonNull() ? obj.get("age").getAsString() : "N/A";
                        String gender = obj.has("gender") && !obj.get("gender").isJsonNull() ? obj.get("gender").getAsString() : "N/A";
                        String phone = obj.has("phone") && !obj.get("phone").isJsonNull() ? obj.get("phone").getAsString() : 
                                      obj.has("mobile") && !obj.get("mobile").isJsonNull() ? obj.get("mobile").getAsString() : "N/A";
                        String email = obj.has("email") && !obj.get("email").isJsonNull() ? obj.get("email").getAsString() : "N/A";
                        String address = obj.has("address") && !obj.get("address").isJsonNull() ? obj.get("address").getAsString() : "N/A";
                        String blood = obj.has("blood_group") && !obj.get("blood_group").isJsonNull() ? obj.get("blood_group").getAsString() : "N/A";
                        String dob = obj.has("dob") && !obj.get("dob").isJsonNull() ? obj.get("dob").getAsString() : "N/A";
                        String createdAt = obj.has("created_at") && !obj.get("created_at").isJsonNull() ? obj.get("created_at").getAsString() : "";
                        String dept = obj.has("department") && !obj.get("department").isJsonNull() ? obj.get("department").getAsString() : "General";
                        String rawDocName = obj.has("doctor_name") && !obj.get("doctor_name").isJsonNull() ? obj.get("doctor_name").getAsString() : "None";
                        String docName = "None";
                        if (!rawDocName.equals("None")) {
                            docName = rawDocName.toLowerCase().startsWith("dr.") || rawDocName.toLowerCase().startsWith("dr ") ? rawDocName : "Dr. " + rawDocName;
                        }
                        String formattedDate = obj.has("formatted_date") && !obj.get("formatted_date").isJsonNull() ? obj.get("formatted_date").getAsString() : "Just Now";

                        patients.add(new Patient(name, dept, pid, age + " / " + gender, formattedDate, age, gender, blood, dob, phone, email, address, docName, formattedDate));
                    }
                    
                    adapter = new PatientAdapter(ManagePatientsActivity.this, patients);
                    binding.rvPatients.setAdapter(adapter);
                } else {
                    Toast.makeText(ManagePatientsActivity.this, "No patient records found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.google.gson.JsonObject>> call, Throwable t) {
                Toast.makeText(ManagePatientsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
