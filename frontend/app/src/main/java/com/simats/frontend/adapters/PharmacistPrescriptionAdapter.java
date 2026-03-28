package com.simats.frontend.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.models.PharmacistPrescription;

import java.util.List;

public class PharmacistPrescriptionAdapter extends RecyclerView.Adapter<PharmacistPrescriptionAdapter.ViewHolder> {

    private Context context;
    private List<PharmacistPrescription> prescriptionList;

    public PharmacistPrescriptionAdapter(Context context, List<PharmacistPrescription> prescriptionList) {
        this.context = context;
        this.prescriptionList = prescriptionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pharmacist_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PharmacistPrescription prescription = prescriptionList.get(position);

        holder.tvPatientName.setText(prescription.getPatientName());
        holder.tvAvatarInitials.setText(prescription.getPatientInitials());
        holder.tvDoctorName.setText("+ " + prescription.getDoctorName()); // Adjusted string formatting
        holder.tvTime.setText(prescription.getTime());
        holder.tvStatusBadge.setText(prescription.getStatus().toUpperCase());

        // Apply dynamic colors
        holder.vStatusIndicator.setBackgroundColor(ContextCompat.getColor(context, prescription.getStatusColorResId()));
        holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, prescription.getStatusTextColorResId()));
        holder.tvStatusBadge.setBackgroundResource(prescription.getStatusBgResId());

        // Dynamic avatar text color based on status or general
        holder.tvAvatarInitials.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary));

        // Hide dispense action if not ISSUED
        if ("ISSUED".equalsIgnoreCase(prescription.getStatus())) {
            holder.btnDispense.setVisibility(View.VISIBLE);
        } else {
            holder.btnDispense.setVisibility(View.GONE);
        }

        holder.btnDispense.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.simats.frontend.PrescriptionDetailActivity.class);
            intent.putExtra("prescription_id", prescription.getId());
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.simats.frontend.PrescriptionDetailActivity.class);
            intent.putExtra("prescription_id", prescription.getId());
            context.startActivity(intent);
        });
    }

    private void dispensePrescription(String id, int position) {
        String idempotencyKey = java.util.UUID.randomUUID().toString();

        com.simats.frontend.network.ApiInterface apiInterface = com.simats.frontend.network.ApiClient.getClient(context)
                .create(com.simats.frontend.network.ApiInterface.class);
        retrofit2.Call<com.google.gson.JsonObject> call = apiInterface.dispensePrescription(id, idempotencyKey);

        call.enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
            @Override
            public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call,
                    retrofit2.Response<com.google.gson.JsonObject> response) {
                if (response.isSuccessful()) {
                    android.widget.Toast.makeText(context, "Prescription Dispensed!", android.widget.Toast.LENGTH_SHORT)
                            .show();
                    // Basic local update
                    prescriptionList.remove(position);
                    notifyItemRemoved(position);
                } else {
                    android.widget.Toast.makeText(context, "Failed to dispense", android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {
                android.widget.Toast
                        .makeText(context, "Network err: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return prescriptionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View vStatusIndicator;
        TextView tvAvatarInitials, tvPatientName, tvDoctorName, tvStatusBadge, tvTime;
        android.widget.ImageView btnDispense;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vStatusIndicator = itemView.findViewById(R.id.vStatusIndicator);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvTime = itemView.findViewById(R.id.tvTime);
            // Cheveon icon now navigates to detail screen
            btnDispense = itemView.findViewById(R.id.ivChevron);
        }
    }
}
