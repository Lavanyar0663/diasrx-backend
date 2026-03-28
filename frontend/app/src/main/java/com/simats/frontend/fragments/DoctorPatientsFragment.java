package com.simats.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.simats.frontend.databinding.FragmentDoctorPatientsBinding;

public class DoctorPatientsFragment extends Fragment {

        private FragmentDoctorPatientsBinding binding;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                        @Nullable Bundle savedInstanceState) {
                binding = FragmentDoctorPatientsBinding.inflate(inflater, container, false);
                if (getContext() == null) return null;

                binding.ivDoctorAvatar.setOnClickListener(v -> {
                        android.content.Intent intent = new android.content.Intent(getContext(), com.simats.frontend.DoctorSettingsActivity.class);
                        startActivity(intent);
                });

                setupTabs();
                setupSearch();
                setupPatientsList();

                return binding.getRoot();
        }

        private void setupTabs() {
                binding.tvTabVisited.setOnClickListener(v -> {
                        if (getContext() == null || binding == null) return;
                        binding.tvTabVisited.setBackgroundResource(com.simats.frontend.R.drawable.bg_card_rounded_white);
                        binding.tvTabVisited.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), com.simats.frontend.R.color.colorPrimary));
                        binding.tvTabVisited.setTypeface(null, android.graphics.Typeface.BOLD);

                        binding.tvTabNew.setBackground(null);
                        binding.tvTabNew.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
                        binding.tvTabNew.setTypeface(null, android.graphics.Typeface.NORMAL);

                        filterPatients(false);
                });

                binding.tvTabNew.setOnClickListener(v -> {
                        if (getContext() == null || binding == null) return;
                        binding.tvTabNew.setBackgroundResource(com.simats.frontend.R.drawable.bg_card_rounded_white);
                        binding.tvTabNew.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), com.simats.frontend.R.color.colorPrimary));
                        binding.tvTabNew.setTypeface(null, android.graphics.Typeface.BOLD);

                        binding.tvTabVisited.setBackground(null);
                        binding.tvTabVisited.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
                        binding.tvTabVisited.setTypeface(null, android.graphics.Typeface.NORMAL);

                        filterPatients(true);
                });
        }

        private void setupSearch() {
                binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                searchPatients(s.toString());
                        }

                        @Override
                        public void afterTextChanged(android.text.Editable s) {}
                });
        }

        private java.util.List<com.simats.frontend.models.DoctorPatient> allPatients = new java.util.ArrayList<>();
        private boolean showNew = false;
        private String currentQuery = "";

        private void filterPatients(boolean isNew) {
                this.showNew = isNew;
                updateList();
        }

        private void searchPatients(String query) {
                this.currentQuery = query.toLowerCase();
                updateList();
        }

        private void updateList() {
                java.util.List<com.simats.frontend.models.DoctorPatient> filtered = new java.util.ArrayList<>();
                for (com.simats.frontend.models.DoctorPatient p : allPatients) {
                        boolean matchesTab = p.isNew() == showNew;
                        boolean matchesQuery = p.getName().toLowerCase().contains(currentQuery) || 
                                             p.getOpdId().toLowerCase().contains(currentQuery);
                        
                        if (matchesTab && matchesQuery) {
                                filtered.add(p);
                        }
                }
                
                com.simats.frontend.adapters.DoctorPatientAdapter adapter = new com.simats.frontend.adapters.DoctorPatientAdapter(
                                getContext(), filtered);
                binding.rvPatients.setAdapter(adapter);
        }

        private void setupPatientsList() {
                binding.rvPatients.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

                com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient
                                .getClient(getContext()).create(com.simats.frontend.network.ApiInterface.class);
                retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call = apiInterface.getPatients();

                call.enqueue(new retrofit2.Callback<java.util.List<com.google.gson.JsonObject>>() {
                        @Override
                        public void onResponse(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call,
                                        retrofit2.Response<java.util.List<com.google.gson.JsonObject>> response) {
                                if (getActivity() == null || binding == null) return;
                                if (response.isSuccessful() && response.body() != null) {
                                    allPatients.clear();
                                    int count = 0;
                                    for (com.google.gson.JsonObject patJson : response.body()) {
                                        if (patJson == null) continue;
                                        
                                        String name = patJson.has("name") && !patJson.get("name").isJsonNull() 
                                                        ? patJson.get("name").getAsString() : "Unknown";

                                        String idStr = patJson.has("id") && !patJson.get("id").isJsonNull() ? patJson.get("id").getAsString() : "0";
                                        String opdId = "OPD #" + idStr;
                                        
                                        int age = patJson.has("age") && !patJson.get("age").isJsonNull() ? patJson.get("age").getAsInt() : 30;
                                        String gender = patJson.has("gender") && !patJson.get("gender").isJsonNull() ? patJson.get("gender").getAsString() : "U";
                                        if (gender.length() > 0) gender = gender.substring(0, 1).toUpperCase();
                                        String ageGender = age + "y / " + gender;

                                        String phone = patJson.has("phone") && !patJson.get("phone").isJsonNull() && !patJson.get("phone").getAsString().isEmpty()
                                                        ? patJson.get("phone").getAsString() 
                                                        : "98800" + String.format("%05d", (count * 1234) % 99999);

                                        String email = patJson.has("email") && !patJson.get("email").isJsonNull() 
                                                        ? patJson.get("email").getAsString() : null;

                                        String initials = "??";
                                        if (name != null && name.length() > 0) {
                                            if (name.length() >= 2) {
                                                initials = name.substring(0, 2).toUpperCase();
                                            } else {
                                                initials = name.toUpperCase();
                                            }
                                        }
                                        
                                        boolean isNew = true;
                                        if (patJson.has("is_visited") && !patJson.get("is_visited").isJsonNull()) {
                                            isNew = patJson.get("is_visited").getAsInt() == 0;
                                        }

                                        String lastVisit = isNew ? "Waiting" : "Visited";

                                        String rawId = patJson.has("id") && !patJson.get("id").isJsonNull() ? patJson.get("id").getAsString() : String.valueOf(count);
                                        allPatients.add(new com.simats.frontend.models.DoctorPatient(
                                                        rawId, name, initials, opdId, ageGender, lastVisit, phone, email,
                                                        com.simats.frontend.R.drawable.bg_card_cyan_light, 
                                                        com.simats.frontend.R.color.colorPrimary, isNew));
                                        count++;
                                    }

                                        updateList();
                                                
                                } else {
                                        if (getContext() != null) {
                                            android.widget.Toast.makeText(getContext(), "Failed to fetch patients",
                                                            android.widget.Toast.LENGTH_SHORT).show();
                                        }
                                }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call,
                                        Throwable t) {
                                if (getContext() != null) {
                                        android.widget.Toast.makeText(getContext(), "Network error: " + t.getMessage(),
                                                        android.widget.Toast.LENGTH_SHORT).show();
                                }
                        }
                });
        }

        @Override
        public void onDestroyView() {
                super.onDestroyView();
                binding = null;
        }
}
