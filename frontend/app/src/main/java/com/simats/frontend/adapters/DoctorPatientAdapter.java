package com.simats.frontend.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.models.DoctorPatient;

import java.util.List;

public class DoctorPatientAdapter extends RecyclerView.Adapter<DoctorPatientAdapter.ViewHolder> {

    private final Context context;
    private final List<DoctorPatient> patientList;

    public DoctorPatientAdapter(Context context, List<DoctorPatient> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorPatient patient = patientList.get(position);

        holder.tvPatientName.setText(patient.getName());
        holder.tvAvatarInitials.setText(patient.getInitials());
        holder.tvOpdId.setText(patient.getOpdId());
        holder.tvAgeGender.setText(patient.getAgeGender());
        holder.tvLastVisit.setText(patient.getLastVisit());
        holder.tvPhone.setText(patient.getPhone());
        if (holder.tvPhoneNew != null) {
            holder.tvPhoneNew.setText(patient.getPhone());
        }

        // Dynamic UI based on New/Visited status
        if (patient.isNew()) {
            holder.tvNewRegBadge.setVisibility(View.VISIBLE);
            holder.llLastVisit.setVisibility(View.GONE);
            holder.llVisitedExtras.setVisibility(View.GONE);
            holder.llNewExtras.setVisibility(View.VISIBLE);
        } else {
            holder.tvNewRegBadge.setVisibility(View.GONE);
            holder.llLastVisit.setVisibility(View.VISIBLE);
            holder.llVisitedExtras.setVisibility(View.VISIBLE);
            holder.llNewExtras.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                if (patient.isNew()) {
                    android.content.Intent intent = new android.content.Intent(context,
                            com.simats.frontend.CreatePrescriptionActivity.class);
                    intent.putExtra("patient_data", patient);
                    context.startActivity(intent);
                } else {
                    android.content.Intent intent = new android.content.Intent(context,
                            com.simats.frontend.PatientMedicationHistoryActivity.class);
                    intent.putExtra("patient_data", patient);
                    context.startActivity(intent);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                android.widget.Toast.makeText(context, "Navigation error: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        if (holder.ivAction != null) {
            holder.ivAction.setOnClickListener(v -> holder.itemView.performClick());
        }
        
        if (holder.ivAddFromHistory != null) {
            holder.ivAddFromHistory.setOnClickListener(v -> {
                if (patient.isNew()) {
                    android.content.Intent intent = new android.content.Intent(context,
                            com.simats.frontend.CreatePrescriptionActivity.class);
                    intent.putExtra("patient_data", patient);
                    context.startActivity(intent);
                } else {
                    holder.itemView.performClick();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View ivPatientAvatar;
        ImageView ivAction, ivAddFromHistory;
        TextView tvAvatarInitials, tvPatientName, tvOpdId, tvAgeGender, tvLastVisit, tvPhone, tvPhoneNew, tvNewRegBadge;
        View llLastVisit, llVisitedExtras, llNewExtras;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPatientAvatar = itemView.findViewById(R.id.ivPatientAvatarContainer);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvOpdId = itemView.findViewById(R.id.tvOpdId);
            tvAgeGender = itemView.findViewById(R.id.tvAgeGender);
            tvLastVisit = itemView.findViewById(R.id.tvLastVisit);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvPhoneNew = itemView.findViewById(R.id.tvPhoneNew);
            tvNewRegBadge = itemView.findViewById(R.id.tvNewRegBadge);
            
            ivAction = itemView.findViewById(R.id.ivAction);
            ivAddFromHistory = itemView.findViewById(R.id.ivAddFromHistory);
            
            llLastVisit = itemView.findViewById(R.id.llLastVisit);
            llVisitedExtras = itemView.findViewById(R.id.llVisitedExtras);
            llNewExtras = itemView.findViewById(R.id.llNewExtras);
        }
    }
}
