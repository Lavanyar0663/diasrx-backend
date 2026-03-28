package com.simats.frontend;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.Locale;
import java.io.File;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simats.frontend.adapters.MedicationAdapter;
import com.simats.frontend.databinding.ActivityCreatePrescriptionBinding;
import com.simats.frontend.models.DoctorPatient;
import com.simats.frontend.models.Medication;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePrescriptionActivity extends AppCompatActivity {

    private ActivityCreatePrescriptionBinding binding;
    private DoctorPatient patient;
    private List<Medication> medicationList = new ArrayList<>();
    private MedicationAdapter medicationAdapter;
    private int currentQty = 0;
    private ApiInterface apiInterface;
    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;
    private List<JsonObject> sourceDrugSuggestions = new ArrayList<>();
    private com.simats.frontend.adapters.MedicationSuggestionAdapter drugSearchAdapter;
    private boolean isDrugSelected = false;
    private String selectedDrugId = "";
    // Removed isSharingFlowActive as we now go direct to success
    
    // AI bilingual text
    private String currentAiExplanation = "";
    private String currentAiExplanationTamil = "";
    private String currentAiReminders = "";
    private String currentAiRemindersTamil = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreatePrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        
        // Get Patient Data
        patient = (DoctorPatient) getIntent().getSerializableExtra("patient_data");
        if (patient != null) {
            setupPatientUI();
        }

        setupSpinners();
        setupRecyclerView();
        setupDrugSearch();
        setupQuantityControls();
        setupChips();

        binding.ivBack.setOnClickListener(v -> finish());

        // Initialize Add to List as disabled
        binding.btnAddToList.setEnabled(false);
        binding.btnAddToList.setAlpha(0.5f);

        // Add to List
        binding.btnAddToList.setOnClickListener(v -> addMedicationToList());

        // Issue
        binding.btnIssue.setOnClickListener(v -> issuePrescription());

        // Add New Treatment
        binding.tvAddNewTreat.setOnClickListener(v -> showAddTreatmentDialog());

        // HANDLE RE-ISSUE PRE-FILL
        if (getIntent().hasExtra("reissue_meds")) {
            List<com.simats.frontend.models.Medication> reissuedMeds = (List<com.simats.frontend.models.Medication>) getIntent().getSerializableExtra("reissue_meds");
            if (reissuedMeds != null) {
                medicationList.addAll(reissuedMeds);
                medicationAdapter.notifyDataSetChanged();
                binding.tvAddedCount.setText(medicationList.size() + " Added");
                updateAiData();
            }
            String reissuedDiag = getIntent().getStringExtra("reissue_diagnosis");
            if (reissuedDiag != null) {
                binding.etDiagnosis.setText(reissuedDiag);
            }
        }
    }

    private void checkInputValidity() {
        String drugName = binding.actvDrugSearch.getText().toString().trim();
        String duration = binding.etDuration.getText().toString().trim();
        boolean isValid = isDrugSelected && !drugName.isEmpty() && currentQty > 0 && !duration.isEmpty();
        binding.btnAddToList.setEnabled(isValid);
        binding.btnAddToList.setAlpha(isValid ? 1.0f : 0.4f);
    }

    private void setupPatientUI() {
        binding.etFullName.setText(patient.getName());
        binding.etMobile.setText(patient.getPhone());
        binding.tvOpdBadge.setText(patient.getOpdId());
        // Extract age from ageGender string (e.g., "58y / M")
        String[] parts = patient.getAgeGender().split("y / ");
        if (parts.length > 0) {
            binding.etAge.setText(parts[0].trim());
        }
        // Pre-fill email from patient profile (doctor can see/clear it before issuing)
        if (patient.getEmail() != null && !patient.getEmail().trim().isEmpty()) {
            binding.etEmail.setText(patient.getEmail().trim());
        }
    }

    private void setupSpinners() {
        // Sex Spinner
        String[] sexOptions = {"Male", "Female", "Other"};
        ArrayAdapter<String> sexAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sexOptions);
        binding.spnSex.setAdapter(sexAdapter);
        if (patient != null && patient.getAgeGender().contains("M")) {
            binding.spnSex.setSelection(0);
        } else if (patient != null && patient.getAgeGender().contains("F")) {
            binding.spnSex.setSelection(1);
        }

        // Frequency Spinner
        String[] freqOptions = {"OD", "BD", "TDS", "QID", "SOS", "As needed"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, freqOptions);
        binding.spnFrequency.setAdapter(freqAdapter);

        // Department Spinner
        String[] deptOptions = {"Select Department", "Oral Medicine", "General Dentistry", "Oral Surgery", "Endodontics", "Periodontics", "Orthodontics", "Prosthodontics", "Pediatric Dentistry"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deptOptions);
        binding.spnDepartment.setAdapter(deptAdapter);

        // Target Pharmacy Spinner
        String[] pharmOptions = {"Select Target Pharmacy", "Main Pharmacy - Block A", "OPD Pharmacy - Block B", "Emergency Pharmacy", "Dental Block Pharmacy", "Staff Clinic Pharmacy"};
        ArrayAdapter<String> pharmAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, pharmOptions);
        binding.spnTargetPharmacy.setAdapter(pharmAdapter);
    }

    private void setupChips() {
        // Treatments
        String[] treatments = {"ANUG", "Bell's Palsy", "Gingivitis", "Periodontitis", "Dental Abscess", "Stomatitis"};
        for (String t : treatments) {
            addChipToGroup(binding.cgTreatments, t);
        }

        // Instructions
        String[] instructions = {"After Meal", "Before Meal", "With Meal", "Empty Stomach", "At Night"};
        for (String i : instructions) {
            addChipToGroup(binding.cgInstructions, i);
        }
    }


    private void setupRecyclerView() {
        medicationAdapter = new MedicationAdapter(this, medicationList);
        medicationAdapter.setOnDeleteListener(position -> {
            if (position >= 0 && position < medicationList.size()) {
                medicationList.remove(position);
                medicationAdapter.notifyItemRemoved(position);
                medicationAdapter.notifyItemRangeChanged(position, medicationList.size());
                binding.tvAddedCount.setText(medicationList.size() + " Added");
                updateAiData();
            }
        });
        binding.rvMedications.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMedications.setAdapter(medicationAdapter);
    }

    private void setupDrugSearch() {
        drugSearchAdapter = new com.simats.frontend.adapters.MedicationSuggestionAdapter(this, sourceDrugSuggestions);
        binding.actvDrugSearch.setAdapter(drugSearchAdapter);

        binding.actvDrugSearch.setOnItemClickListener((parent, view, position, id) -> {
            JsonObject selected = (JsonObject) parent.getItemAtPosition(position);
            if (selected.has("no_results")) {
                binding.actvDrugSearch.setText("");
                isDrugSelected = false;
                checkInputValidity();
                return;
            }
            
            isDrugSelected = true;
            selectedDrugId = selected.has("id") && !selected.get("id").isJsonNull() ? selected.get("id").getAsString() : "0";
            checkInputValidity();
            
            String fullName = selected.get("name").getAsString();
            // Simple heuristic to extract strength if present
            int firstDigit = -1;
            for(int i=0; i<fullName.length(); i++) {
                if(Character.isDigit(fullName.charAt(i))) {
                    firstDigit = i;
                    break;
                }
            }
            if (firstDigit != -1) {
                String strength = fullName.substring(firstDigit).trim();
                binding.etStrength.setText(strength);
            }
        });

        binding.actvDrugSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                if (s.length() >= 2 && !binding.actvDrugSearch.isPerformingCompletion()) {
                    searchRunnable = () -> searchDrugs(s.toString());
                    searchHandler.postDelayed(searchRunnable, 300); 
                }
                
                // CRITICAL: Any time the text changes from the KEYBOARD (not from completion),
                // we MUST reset isDrugSelected to false.
                if (!binding.actvDrugSearch.isPerformingCompletion()) {
                    isDrugSelected = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkInputValidity();
            }
        });

        binding.etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkInputValidity();
            }
        });
    }

    private void searchDrugs(String query) {
        apiInterface.searchDrugs(query).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sourceDrugSuggestions.clear();
                    sourceDrugSuggestions.addAll(response.body());
                    
                    drugSearchAdapter.updateData(sourceDrugSuggestions);
                    drugSearchAdapter.getFilter().filter(binding.actvDrugSearch.getText().toString());
                    binding.actvDrugSearch.showDropDown();
                } else {
                    Toast.makeText(CreatePrescriptionActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                android.util.Log.e("DIAS_RX", "Search failed", t);
                Toast.makeText(CreatePrescriptionActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupQuantityControls() {
        binding.btnPlusQty.setOnClickListener(v -> {
            currentQty++;
            updateQtyDisplay();
            checkInputValidity();
        });

        binding.btnMinusQty.setOnClickListener(v -> {
            if (currentQty > 0) {
                currentQty--;
                updateQtyDisplay();
                checkInputValidity();
            }
        });
    }

    private void addMedicationToList() {
        String drugName = binding.actvDrugSearch.getText().toString();
        String strength = binding.etStrength.getText().toString();
        int qty = currentQty;
        String freq = binding.spnFrequency.getSelectedItem().toString();
        
        String instructions = "After Meal";
        int checkedChipId = binding.cgInstructions.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip chip = findViewById(checkedChipId);
            instructions = chip.getText().toString();
        }

        if (drugName.isEmpty() || !isDrugSelected) {
            showCustomSnackbar("Please select a drug from the suggestions");
            return;
        }

        // Duration: numeric input → format as "X Days"
        String durationRaw = binding.etDuration.getText().toString().trim();
        String duration;
        if (!durationRaw.isEmpty()) {
            duration = durationRaw + " Days";
        } else {
            duration = "7 Days"; // Default fallback
        }

        Medication med = new Medication(selectedDrugId, drugName, strength, qty, freq, instructions, duration);
        medicationList.add(med);
        int newPos = medicationList.size() - 1;
        medicationAdapter.notifyItemInserted(newPos);
        binding.tvAddedCount.setText(medicationList.size() + " Added");
        showMinimalSuccessPopup("Medicine added ✅");

        // Clear inputs for next entry
        binding.actvDrugSearch.setText("");
        isDrugSelected = false; // Reset selection flag
        binding.etStrength.setText("");
        binding.etDuration.setText("");
        currentQty = 0;
        updateQtyDisplay();
        checkInputValidity();
        
        updateAiData();

        // Scroll to show new item, then return focus to drug search field
        binding.rvMedications.post(() -> {
            binding.rvMedications.requestLayout();
        });
        
        // Return focus to drug name field for fast entry
        binding.actvDrugSearch.requestFocus();
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(binding.actvDrugSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
    }

    private void updateQtyDisplay() {
        binding.tvQtyDisplay.setText(String.valueOf(currentQty));
    }


    private void issuePrescription() {
        if (medicationList.isEmpty()) {
            Toast.makeText(this, "Add at least one medication", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.spnTargetPharmacy.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a target internal pharmacy", Toast.LENGTH_SHORT).show();
            return;
        }

        if (binding.spnDepartment.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!binding.cbCertify.isChecked()) {
            Toast.makeText(this, "Please certify the prescription first.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable button and show progress overlay
        binding.btnIssue.setEnabled(false);
        binding.loadingOverlay.setVisibility(View.VISIBLE);

        // Construct Prescription JSON for Backend
        JsonObject body = new JsonObject();
        body.addProperty("patient_id", patient.getId());
        String diagnosis = binding.etDiagnosis.getText().toString();
        if (diagnosis.isEmpty()) diagnosis = "Clinical Consultation";
        body.addProperty("diagnosis", diagnosis);
        body.addProperty("remarks", "Standard medical prescription generated via AI Assistant.");
        
        com.google.gson.JsonArray drugsArray = new com.google.gson.JsonArray();
        for (Medication m : medicationList) {
            JsonObject dObj = new JsonObject();
            dObj.addProperty("drug_id", m.getId());
            dObj.addProperty("quantity", m.getQuantity());
            dObj.addProperty("frequency", m.getFrequency());
            dObj.addProperty("dosage", m.getStrength()); // Map Strength to Dosage
            dObj.addProperty("duration", m.getDuration());
            dObj.addProperty("instructions", m.getInstructions());
            drugsArray.add(dObj);
        }
        body.add("drugs", drugsArray);

        apiInterface.createFullPrescription(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    navigateToSuccess();
                } else {
                    binding.btnIssue.setEnabled(true);
                    binding.loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(CreatePrescriptionActivity.this, "Failed to save prescription: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnIssue.setEnabled(true);
                binding.loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(CreatePrescriptionActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void triggerShareFlow() {
        binding.btnIssue.setEnabled(true);
        binding.btnIssue.setText("Issue Prescription");

        File pdfFile = generatePrescriptionPdf();
        if (pdfFile == null) {
            Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri contentUri = androidx.core.content.FileProvider.getUriForFile(this, 
                getPackageName() + ".fileprovider", pdfFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        // Prefilled text for convenience
        String message = "Medical Prescription for " + patient.getName() + " (OPD: " + patient.getOpdId() + ")";
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Prescription - " + patient.getName());

        startActivity(Intent.createChooser(shareIntent, "Share Prescription PDF"));
    }

    private File generatePrescriptionPdf() {
        android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
        android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);

        android.graphics.Canvas canvas = page.getCanvas();
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setTextSize(14);
        
        int y = 50;
        paint.setFakeBoldText(true);
        paint.setTextSize(18);
        canvas.drawText("DIAS Rx - MEDICAL PRESCRIPTION", 150, y, paint);
        
        y += 40;
        paint.setFakeBoldText(false);
        paint.setTextSize(12);
        canvas.drawText("Date: " + new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(new java.util.Date()), 450, y, paint);
        
        y += 30;
        paint.setFakeBoldText(true);
        canvas.drawText("PATIENT INFORMATION", 50, y, paint);
        y += 20;
        paint.setFakeBoldText(false);
        canvas.drawText("Name: " + patient.getName(), 50, y, paint);
        canvas.drawText("OPD ID: " + patient.getOpdId(), 300, y, paint);
        y += 20;
        canvas.drawText("Age/Sex: " + patient.getAgeGender(), 50, y, paint);
        canvas.drawText("Mobile: " + patient.getPhone(), 300, y, paint);
        
        y += 40;
        paint.setFakeBoldText(true);
        canvas.drawText("CLINICAL DATA", 50, y, paint);
        y += 20;
        paint.setFakeBoldText(false);
        String diagnosis = binding.etDiagnosis.getText().toString();
        if (diagnosis.isEmpty()) diagnosis = "Clinical Consultation";
        canvas.drawText("Diagnosis: " + diagnosis, 50, y, paint);
        y += 20;
        canvas.drawText("Department: " + binding.spnDepartment.getSelectedItem().toString(), 50, y, paint);
        
        y += 40;
        paint.setFakeBoldText(true);
        canvas.drawText("MEDICATIONS", 50, y, paint);
        y += 10;
        paint.setStrokeWidth(1);
        canvas.drawLine(50, y, 545, y, paint);
        
        y += 25;
        for (Medication m : medicationList) {
            paint.setFakeBoldText(true);
            canvas.drawText(m.getName() + " (" + m.getStrength() + ")", 50, y, paint);
            canvas.drawText("Qty: " + m.getQuantity(), 450, y, paint);
            y += 18;
            paint.setFakeBoldText(false);
            canvas.drawText(m.getFrequency() + " - " + m.getInstructions(), 70, y, paint);
            y += 25;
            
            if (y > 750) break; // Simple page break protection
        }
        
        y += 20;
        if (!currentAiExplanation.isEmpty()) {
            paint.setFakeBoldText(true);
            canvas.drawText("SIMPLIFIED INSTRUCTIONS (AI)", 50, y, paint);
            y += 20;
            paint.setFakeBoldText(false);
            paint.setTextSize(10);
            String[] lines = currentAiExplanation.split("\n");
            for(String line : lines) {
                canvas.drawText(line, 50, y, paint);
                y += 15;
            }
        }

        y = 800;
        paint.setTextSize(10);
        canvas.drawText("Note: This is a digitally generated prescription for academic prototype demonstration.", 120, y, paint);

        document.finishPage(page);

        File pdfFile = new File(getExternalCacheDir(), "Prescription_" + patient.getOpdId().replace("#", "") + ".pdf");
        try {
            document.writeTo(new java.io.FileOutputStream(pdfFile));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            document.close();
        }
        return pdfFile;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void navigateToSuccess() {
        Intent intent = new Intent(this, PrescriptionSuccessActivity.class);
        intent.putExtra("patient_name", patient.getName());
        intent.putExtra("opd_id", patient.getOpdId());
        intent.putExtra("age_gender", patient.getAgeGender());
        intent.putExtra("phone", patient.getPhone());
        // Use whatever email the doctor sees in the form — empty if cleared or not provided
        String formEmail = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        intent.putExtra("email", formEmail);
        intent.putExtra("med_count", medicationList.size());
        intent.putExtra("medication_list", (java.io.Serializable) medicationList);
        intent.putExtra("diagnosis", binding.etDiagnosis.getText().toString());
        intent.putExtra("department", binding.spnDepartment.getSelectedItem().toString());
        // Pass all AI bilingual content for PDF inclusion
        intent.putExtra("ai_explanation", currentAiExplanation);
        intent.putExtra("ai_explanation_tamil", currentAiExplanationTamil);
        intent.putExtra("ai_reminders", currentAiReminders);
        intent.putExtra("ai_reminders_tamil", currentAiRemindersTamil);
        startActivity(intent);
        finish();
    }

    private void showAddTreatmentDialog() {
        com.simats.frontend.utils.DialogHelper.showInputDialog(
                this,
                "Add New Treatment",
                "Enter treatment name",
                "Add",
                "Cancel",
                treatment -> {
                    addChipToGroup(binding.cgTreatments, treatment);
                    Toast.makeText(this, "Treatment " + treatment + " added", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void addChipToGroup(ChipGroup group, String title) {
        Chip chip = (Chip) LayoutInflater.from(this).inflate(R.layout.item_chip, group, false);
        chip.setText(title);
        
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chip.setChipBackgroundColorResource(R.color.colorPrimary);
                chip.setTextColor(getResources().getColor(R.color.white));
                chip.setChipStrokeWidth(0);
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(getResources().getColor(R.color.colorSecondary));
                chip.setChipStrokeWidth(3); 
            }
        });

        group.addView(chip);
    }

    private void updateAiData() {
        if (medicationList.isEmpty()) {
            currentAiExplanation = "Add medications to generate simplified instructions.";
            currentAiExplanationTamil = "விளக்கத்தை உருவாக்க மருந்துகளைச் சேர்க்கவும்.";
            currentAiReminders = "Schedule not generated yet.";
            currentAiRemindersTamil = "அட்டவணை இன்னும் உருவாக்கப்படவில்லை.";
        } else {
            StringBuilder expBuilder = new StringBuilder();
            StringBuilder expTamBuilder = new StringBuilder();
            StringBuilder remBuilder = new StringBuilder();
            StringBuilder remTamBuilder = new StringBuilder();
            
            for (Medication m : medicationList) {
                String name = m.getName();
                String duration = m.getDuration() != null && !m.getDuration().isEmpty() ? m.getDuration() : "";
                String durSuffix = duration.isEmpty() ? "" : " for " + duration;
                String durTamil = duration.isEmpty() ? "" : duration.replace("Days", "நாட்கள்").replace("Day", "நாள்").replace("7 நாட்கள்", "1 வாரம்");
                String durSuffixTam = durTamil.isEmpty() ? "" : " " + durTamil + " வரை";

                String freq = m.getFrequency();
                String instr = m.getInstructions().toLowerCase();
                String instrTam = translateToTamil(instr);

                if (freq.equals("OD")) {
                    expBuilder.append("Take ").append(name).append(" once daily").append(durSuffix).append(" (").append(instr).append(").\n");
                    expTamBuilder.append(name).append(" மாத்திரையை ஒரு முறை மட்டும் ").append(durSuffixTam).append(" (").append(instrTam).append(") உட்கொள்ளவும்.\n");
                    remBuilder.append("• ").append(name).append(": 09:00 AM (").append(instr).append(")\n");
                    remTamBuilder.append("• ").append(name).append(": காலை 09:00 மணி (").append(instrTam).append(")\n");
                } else if (freq.equals("BD")) {
                    expBuilder.append("Take ").append(name).append(" twice daily").append(durSuffix).append(" (").append(instr).append(").\n");
                    expTamBuilder.append(name).append(" மாத்திரையை தினம் இரு முறை ").append(durSuffixTam).append(" (").append(instrTam).append(") உட்கொள்ளவும்.\n");
                    remBuilder.append("• ").append(name).append(": 09:00 AM, 09:00 PM (").append(instr).append(")\n");
                    remTamBuilder.append("• ").append(name).append(": காலை 09:00, இரவு 09:00 (").append(instrTam).append(")\n");
                } else if (freq.equals("TDS")) {
                    expBuilder.append("Take ").append(name).append(" three times daily").append(durSuffix).append(" (").append(instr).append(").\n");
                    expTamBuilder.append(name).append(" மாத்திரையை தினம் மூன்று முறை ").append(durSuffixTam).append(" (").append(instrTam).append(") உட்கொள்ளவும்.\n");
                    remBuilder.append("• ").append(name).append(": 09:00 AM, 02:00 PM, 09:00 PM (").append(instr).append(")\n");
                    remTamBuilder.append("• ").append(name).append(": காலை 09:00, மதியம் 02:00, இரவு 09:00 (").append(instrTam).append(")\n");
                } else if (freq.equals("QID")) {
                    expBuilder.append("Take ").append(name).append(" four times daily").append(durSuffix).append(" (").append(instr).append(").\n");
                    expTamBuilder.append(name).append(" மாத்திரையை தினம் நான்கு முறை ").append(durSuffixTam).append(" (").append(instrTam).append(") உட்கொள்ளவும்.\n");
                    remBuilder.append("• ").append(name).append(": 08:00 AM, 12:00 PM, 04:00 PM, 08:00 PM (").append(instr).append(")\n");
                    remTamBuilder.append("• ").append(name).append(": காலை 08:00, மதியம் 12:00, மாலை 04:00, இரவு 08:00 (").append(instrTam).append(")\n");
                } else if (freq.equals("SOS") || freq.equals("As needed")) {
                    expBuilder.append("Take ").append(name).append(" as needed for symptoms").append(durSuffix).append(" (").append(instr).append(").\n");
                    expTamBuilder.append(name).append(" மாத்திரையை தேவைப்படும் போது மட்டும் ").append(durSuffixTam).append(" (").append(instrTam).append(") உட்கொள்ளவும்.\n");
                    remBuilder.append("• ").append(name).append(": Take only when required (").append(instr).append(")\n");
                    remTamBuilder.append("• ").append(name).append(": தேவைப்படும் போது மட்டும் (").append(instrTam).append(")\n");
                } else {
                    expBuilder.append("Take ").append(name).append(" ").append(freq).append(durSuffix).append(" (").append(instr).append(").\n");
                    expTamBuilder.append(name).append(" மாத்திரையை ").append(freq).append(" ").append(durSuffixTam).append(" (").append(instrTam).append(") உட்கொள்ளவும்.\n");
                    remBuilder.append("• ").append(name).append(": ").append(freq).append(" (").append(instr).append(")\n");
                    remTamBuilder.append("• ").append(name).append(": ").append(freq).append(" (").append(instrTam).append(")\n");
                }
            }
            currentAiExplanation = expBuilder.toString().trim();
            currentAiExplanationTamil = expTamBuilder.toString().trim();
            currentAiReminders = remBuilder.toString().trim();
            currentAiRemindersTamil = remTamBuilder.toString().trim();
        }
        
        binding.tvAiExplanationText.setText(currentAiExplanation);
        binding.tvAiExplanationTamil.setText(currentAiExplanationTamil);
        binding.tvAiRemindersText.setText(currentAiReminders);
        binding.tvAiRemindersTamil.setText(currentAiRemindersTamil);
    }
    
    // Kept for basic instruction translation
    private String translateToTamil(String text) {
        if(text == null) return "";
        String translated = text.toLowerCase();
        
        translated = translated.replace("after meal", "சாப்பாட்டிற்கு பின்");
        translated = translated.replace("before meal", "சாப்பாட்டிற்கு முன்");
        translated = translated.replace("with meal", "உணவுடன்");
        translated = translated.replace("empty stomach", "வெறும் வயிற்றில்");
        translated = translated.replace("at night", "இரவில்");
        
        return translated;
    }
    
    // ── Custom Teal Snackbar Utility ──────────────────────────────────────────
    private void showCustomSnackbar(String message) {
        // Floating premium Snackbar for ERRORS (High contrast Teal)
        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(
                binding.getRoot(), message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        
        android.view.View snackBarView = snackbar.getView();
        snackBarView.setBackgroundResource(R.drawable.bg_custom_snackbar);
        snackBarView.setBackgroundTintList(null); 
        
        android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) snackBarView.getLayoutParams();
        int marginSides = (int) (48 * getResources().getDisplayMetrics().density);
        int marginBottom = (int) (64 * getResources().getDisplayMetrics().density);
        params.setMargins(marginSides, 0, marginSides, marginBottom);
        
        android.widget.TextView tv = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) {
            tv.setTextColor(android.graphics.Color.WHITE);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        snackbar.show();
    }

    private void showSuccessPopup(String message) {
        // Premium centered success card - a discrete pop up like high-end apps
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_success_popup);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Non-blocking but visible dim
            dialog.getWindow().setDimAmount(0.1f); 
        }

        TextView tv = dialog.findViewById(R.id.tvPopupMessage);
        if (tv != null) tv.setText(message);

        dialog.setCancelable(true);
        dialog.show();

        // Auto-dismiss after 1.5 seconds for a snappy feel
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) dialog.dismiss();
        }, 1500);
    }

    private void showMinimalSuccessPopup(String message) {
        // Custom DIAS Rx minimal popup at bottom
        View layout = getLayoutInflater().inflate(R.layout.layout_minimal_success, null);
        TextView tv = layout.findViewById(R.id.tvMessage);
        tv.setText(message);

        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), "", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);
        com.google.android.material.snackbar.Snackbar.SnackbarLayout snackLayout = (com.google.android.material.snackbar.Snackbar.SnackbarLayout) snackbar.getView();
        snackLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        snackLayout.setPadding(0, 0, 0, 80); // bottom margin
        snackLayout.addView(layout, 0);
        
        // Adjust gravity to bottom center
        android.view.View view = snackLayout;
        android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = android.widget.FrameLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        view.setLayoutParams(params);
        
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

