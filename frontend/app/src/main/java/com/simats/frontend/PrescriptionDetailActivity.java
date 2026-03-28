package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityPrescriptionDetailBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionDetailActivity extends AppCompatActivity {

    private ActivityPrescriptionDetailBinding binding;
    private String prescriptionId;
    private JsonObject prescriptionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        prescriptionId = getIntent().getStringExtra("prescription_id");

        // Set dispense logic: Always use API flow if ID is present
        binding.btnDispense.setOnClickListener(v -> {
            if (prescriptionId != null) {
                dispensePrescription();
            } else {
                Toast.makeText(this, "Error: No prescription ID", Toast.LENGTH_SHORT).show();
            }
        });

        // Always prioritize API fetch if ID is present
        if (prescriptionId != null) {
            fetchPrescriptionDetails();
        } else if (getIntent().getStringExtra("mock_name") != null) {
            populateFromExtras();
        } else {
            Toast.makeText(this, "Error: No prescription data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    /** Populate UI from the intent extras passed by PrescriptionHistoryAdapter */
    private void populateFromExtras() {
        String name    = getIntent().getStringExtra("mock_name");
        String rxId    = getIntent().getStringExtra("mock_rx_id");
        String doctor  = getIntent().getStringExtra("mock_doctor");
        String time    = getIntent().getStringExtra("mock_time");
        String status  = getIntent().getStringExtra("mock_status");

        binding.tvPrescriptionId.setText(rxId != null ? rxId : "#RX-0000");
        binding.tvPatientNameDetail.setText(name);
        binding.tvPatientDetailsDetail.setText("Patient Prescription Record");
        binding.tvDoctorNameDetail.setText(doctor != null ? doctor : "Dr. Unknown");
        binding.tvDoctorDeptDetail.setText("General & Dental Practice");
        binding.tvDatePrescribed.setText(time != null ? time : "Today");
        binding.tvValidUntil.setText("Valid: 7 Days");

        // Normalize status badge
        if ("Dispensed".equalsIgnoreCase(status)) {
            binding.tvStatusBadgeDetail.setText("• Dispensed");
            binding.tvStatusBadgeDetail.setTextColor(android.graphics.Color.parseColor("#059669"));
            binding.tvStatusBadgeDetail.setBackgroundResource(R.drawable.bg_badge_light_green);
            binding.btnDispense.setVisibility(View.GONE);
        } else {
            binding.tvStatusBadgeDetail.setText("• Pending Dispense");
            binding.tvStatusBadgeDetail.setTextColor(android.graphics.Color.parseColor("#00BCD4"));
            binding.tvStatusBadgeDetail.setBackgroundResource(R.drawable.bg_badge_light_blue);
        }

        // Per-patient medicines mapping - No longer using mock data
        binding.tvMedicationsCount.setText("Medications (0)");
        binding.rvMedications.setAdapter(new GenericDrugAdapter(new com.google.gson.JsonArray()));
    }

    private void fetchPrescriptionDetails() {
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.getPrescriptionDetails(prescriptionId);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    prescriptionData = response.body();
                    populateDetails();
                } else {
                    Toast.makeText(PrescriptionDetailActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(PrescriptionDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateDetails() {
        String idPrefix = "#RX-";
        binding.tvPrescriptionId.setText(idPrefix + prescriptionId);
        
        String patientName = prescriptionData.has("patient_name") ? prescriptionData.get("patient_name").getAsString() : "Unknown";
        String patientDisplayId = prescriptionData.has("patient_display_id") ? prescriptionData.get("patient_display_id").getAsString() : "P-00000";
        int age = prescriptionData.has("age") ? prescriptionData.get("age").getAsInt() : 0;
        String gender = prescriptionData.has("gender") ? prescriptionData.get("gender").getAsString() : "N/A";

        binding.tvPatientNameDetail.setText(patientName);
        binding.tvPatientDetailsDetail.setText("ID: " + patientDisplayId + " • " + age + " Yrs / " + gender);

        String doctorName = prescriptionData.has("doctor_name") ? prescriptionData.get("doctor_name").getAsString() : "Dr. Unknown";
        String doctorRole = prescriptionData.has("doctor_role") ? prescriptionData.get("doctor_role").getAsString() : "Dept.";
        
        binding.tvDoctorNameDetail.setText(doctorName);
        binding.tvDoctorDeptDetail.setText(doctorRole);

        // Created At for date
        String createdAt = prescriptionData.has("created_at") ? prescriptionData.get("created_at").getAsString() : "Today";
        // Simple trim for date only if timestamp
        if (createdAt.contains("T")) createdAt = createdAt.split("T")[0];
        binding.tvDatePrescribed.setText(createdAt);
        binding.tvValidUntil.setText("Expiry: 7 Days");

        String status = prescriptionData.has("status") ? prescriptionData.get("status").getAsString() : "PENDING";
        binding.tvStatusBadgeDetail.setText("• " + status.toUpperCase());

        if (!"ISSUED".equalsIgnoreCase(status) && !"PENDING".equalsIgnoreCase(status) && !"CREATED".equalsIgnoreCase(status) && !"".equals(status)) {
            binding.btnDispense.setVisibility(View.GONE);
        }

        // Drugs
        if (prescriptionData.has("drugs") && prescriptionData.get("drugs").isJsonArray()) {
            JsonArray drugsLine = prescriptionData.getAsJsonArray("drugs");
            binding.tvMedicationsCount.setText("Medications (" + drugsLine.size() + ")");
            setupDrugsRecyclerView(drugsLine);
        }
    }

    private void setupDrugsRecyclerView(JsonArray drugs) {
        binding.rvMedications.setLayoutManager(new LinearLayoutManager(this));
        // Simple adapter for drugs
        GenericDrugAdapter adapter = new GenericDrugAdapter(drugs);
        binding.rvMedications.setAdapter(adapter);
    }

    private void dispensePrescription() {
        String idempotencyKey = UUID.randomUUID().toString();
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.dispensePrescription(prescriptionId, idempotencyKey);

        binding.btnDispense.setEnabled(false);
        binding.btnDispense.setText("Processing...");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(PrescriptionDetailActivity.this, DispenseSuccessActivity.class);
                    intent.putExtra("patient_name", binding.tvPatientNameDetail.getText().toString());
                    intent.putExtra("prescription_id", prescriptionId);
                    startActivity(intent);
                    finish();
                } else {
                    binding.btnDispense.setEnabled(true);
                    binding.btnDispense.setText("Mark as Dispensed");
                    String errorMsg = "Failed to dispense";
                    try {
                        if (response.errorBody() != null) {
                            JsonObject errorObj = com.google.gson.JsonParser.parseString(response.errorBody().string()).getAsJsonObject();
                            if (errorObj.has("message")) errorMsg = errorObj.get("message").getAsString();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(PrescriptionDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                binding.btnDispense.setEnabled(true);
                binding.btnDispense.setText("Mark as Dispensed");
                Toast.makeText(PrescriptionDetailActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Quick Inner Adapter for drugs
    private class GenericDrugAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<GenericDrugAdapter.ViewHolder> {
        private JsonArray drugs;
        GenericDrugAdapter(JsonArray drugs) { this.drugs = drugs; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup p, int vt) {
            View v = android.view.LayoutInflater.from(p.getContext()).inflate(R.layout.item_prescription_detail_drug, p, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            JsonObject d = drugs.get(pos).getAsJsonObject();
            h.name.setText(d.has("drug_name") ? d.get("drug_name").getAsString() : "Drug");
            
            String dosage = d.has("dosage") && !d.get("dosage").isJsonNull() ? d.get("dosage").getAsString() : "";
            String type = d.has("drug_type") && !d.get("drug_type").isJsonNull() ? d.get("drug_type").getAsString() : "";
            h.desc.setText(dosage + " " + type);
            
            h.qty.setText(d.has("quantity") ? d.get("quantity").getAsString() : "0");
            h.freq.setText(d.has("frequency") ? d.get("frequency").getAsString() : "-");
            
            String duration = d.has("duration") && !d.get("duration").isJsonNull() ? d.get("duration").getAsString() : "As prescribed";
            h.dur.setText(duration);
            
            String instructions = d.has("instructions") && !d.get("instructions").isJsonNull() ? d.get("instructions").getAsString() : "No special instructions";
            h.note.setText("Note: " + instructions);
        }
        @Override public int getItemCount() { return drugs.size(); }
        class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            android.widget.TextView name, desc, qty, freq, dur, note;
            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.tvDrugName);
                desc = v.findViewById(R.id.tvDrugDescription);
                qty = v.findViewById(R.id.tvDrugQty);
                freq = v.findViewById(R.id.tvDrugFreq);
                dur = v.findViewById(R.id.tvDrugDur);
                note = v.findViewById(R.id.tvDrugNote);
            }
        }
    }
}
