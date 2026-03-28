package com.simats.frontend.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.PatientMedicationHistoryActivity;
import com.simats.frontend.R;
import com.simats.frontend.models.DoctorPatient;

import java.util.List;

public class PharmacistPatientAdapter extends RecyclerView.Adapter<PharmacistPatientAdapter.ViewHolder> {

    private final Context context;
    private final List<DoctorPatient> patientList;

    public PharmacistPatientAdapter(Context context, List<DoctorPatient> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pharmacist_patient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorPatient patient = patientList.get(position);

        holder.tvPatientName.setText(patient.getName());
        holder.tvAvatarInitials.setText(patient.getInitials());
        holder.tvLastVisitTime.setText(patient.getVisitTime());
        
        String metadata = patient.getAgeGender() + " • " + patient.getOpdId();
        holder.tvPatientMetadata.setText(metadata);
        
        holder.tvLastVisitDate.setText("Last Visit: " + patient.getLastVisit());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PatientMedicationHistoryActivity.class);
            intent.putExtra("patient_data", patient);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitials, tvPatientName, tvLastVisitTime, tvPatientMetadata, tvLastVisitDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvLastVisitTime = itemView.findViewById(R.id.tvLastVisitTime);
            tvPatientMetadata = itemView.findViewById(R.id.tvPatientMetadata);
            tvLastVisitDate = itemView.findViewById(R.id.tvLastVisitDate);
        }
    }
}
