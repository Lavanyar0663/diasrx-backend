package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simats.frontend.adapters.PharmacistAdapter;
import com.simats.frontend.databinding.ActivityManagePharmacistsBinding;
import com.simats.frontend.models.Pharmacist;

import java.util.ArrayList;
import java.util.List;

public class ManagePharmacistsActivity extends AppCompatActivity {

    private ActivityManagePharmacistsBinding binding;
    private PharmacistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManagePharmacistsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        binding.rvPharmacists.setLayoutManager(new LinearLayoutManager(this));

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

        // Fab click
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewStaffActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPharmacists();
    }

    private void loadPharmacists() {
        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(this).create(com.simats.frontend.network.ApiInterface.class);
        apiInterface.getPharmacists().enqueue(new retrofit2.Callback<List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.google.gson.JsonObject>> call, retrofit2.Response<List<com.google.gson.JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pharmacist> pharmacistList = new ArrayList<>();
                    for (com.google.gson.JsonObject json : response.body()) {
                        String id = json.has("id") && !json.get("id").isJsonNull() ? json.get("id").getAsString() : "";
                        String name = json.has("name") && !json.get("name").isJsonNull() ? json.get("name").getAsString() : "Unknown";
                        String phone = json.has("phone") && !json.get("phone").isJsonNull() ? json.get("phone").getAsString() : "N/A";
                        String email = json.has("email") && !json.get("email").isJsonNull() ? json.get("email").getAsString() : "N/A";
                        
                        pharmacistList.add(new Pharmacist(id, name, "ACTIVE", phone, email, "5 Years"));
                    }
                    adapter = new PharmacistAdapter(ManagePharmacistsActivity.this, pharmacistList);
                    binding.rvPharmacists.setAdapter(adapter);
                } else {
                    Toast.makeText(ManagePharmacistsActivity.this, "Failed to load pharmacists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.google.gson.JsonObject>> call, Throwable t) {
                Toast.makeText(ManagePharmacistsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
