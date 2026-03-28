package com.simats.frontend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityEditProfileBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;
import com.simats.frontend.network.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private SessionManager sessionManager;
    private ApiInterface apiInterface;
    private Uri selectedImageUri;
    private String selectedDepartment = "";

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        binding.ivProfileImage.setImageURI(selectedImageUri);
                        binding.ivProfileImage.setPadding(0, 0, 0, 0);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        apiInterface = ApiClient.getClient(this).create(ApiInterface.class);

        setupFields();
        setupDepartmentSpinner();

        // Back button navigation
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangePhoto.setOnClickListener(v -> openGallery());
        binding.tvChangePhoto.setOnClickListener(v -> openGallery());

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupFields() {
        binding.etName.setText(sessionManager.getName());
        binding.etTitle.setText(sessionManager.getTitle());
        binding.etPhone.setText(sessionManager.getPhone());

        String avatar = sessionManager.getAvatar();
        if (avatar != null && !avatar.isEmpty()) {
            try {
                binding.ivProfileImage.setImageURI(Uri.parse(avatar));
                binding.ivProfileImage.setPadding(0, 0, 0, 0);
            } catch (Exception e) {
                binding.ivProfileImage.setImageResource(R.drawable.ic_person);
            }
        }
    }

    private void setupDepartmentSpinner() {
        String[] departments = {
                "Select Department",
                "Oral Medicine",
                "General Dentistry",
                "Oral Surgery",
                "Endodontics",
                "Periodontics",
                "Orthodontics",
                "Prosthodontics",
                "Pediatric Dentistry"
        };

        // Use a simple spinner adapter with white background
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, departments) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((android.widget.TextView) v).setTextColor(getResources().getColor(R.color.colorSecondary, null));
                ((android.widget.TextView) v).setTextSize(16f);
                v.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner spinner = (Spinner) binding.etSpec;
        spinner.setAdapter(adapter);
        spinner.setBackground(null);

        // Pre-select saved department
        String savedSpec = sessionManager.getSpec();
        if (savedSpec != null && !savedSpec.isEmpty()) {
            for (int i = 0; i < departments.length; i++) {
                if (departments[i].equals(savedSpec)) {
                    spinner.setSelection(i);
                    selectedDepartment = savedSpec;
                    break;
                }
            }
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof android.widget.TextView) {
                    ((android.widget.TextView) view).setTextColor(getResources().getColor(R.color.colorSecondary, null));
                }
                selectedDepartment = position == 0 ? "" : departments[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDepartment = "";
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String title = binding.etTitle.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String avatar = selectedImageUri != null ? selectedImageUri.toString() : sessionManager.getAvatar();

        // Validation — required fields
        if (name.isEmpty()) {
            binding.etName.setError("Full name is required");
            binding.etName.requestFocus();
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDepartment.isEmpty()) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("Saving...");

        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("professional_title", title);
        body.addProperty("specialization", selectedDepartment);
        body.addProperty("phone", phone);
        body.addProperty("avatar_url", avatar != null ? avatar : "");

        apiInterface.updateDoctorProfile(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("Save Changes");

                if (response.isSuccessful()) {
                    sessionManager.updateProfile(name, phone, title, selectedDepartment, avatar != null ? avatar : "");
                    Intent successIntent = new Intent(EditProfileActivity.this, ProfileUpdatedSuccessActivity.class);
                    startActivity(successIntent);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this,
                            "Update failed (" + response.code() + "). Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnSave.setEnabled(true);
                binding.btnSave.setText("Save Changes");
                Toast.makeText(EditProfileActivity.this,
                        "Network error. Check your connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
