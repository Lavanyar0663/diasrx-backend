package com.simats.frontend.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.simats.frontend.models.PrescriptionHistoryItem;

import java.util.List;

public class PrescriptionHistoryAdapter extends RecyclerView.Adapter<PrescriptionHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<PrescriptionHistoryItem> historyList;

    public PrescriptionHistoryAdapter(Context context, List<PrescriptionHistoryItem> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_prescription_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrescriptionHistoryItem item = historyList.get(position);

        holder.tvPatientName.setText(item.getName());
        holder.tvAvatarInitials.setText(item.getInitials());
        holder.tvSubtitle.setText(item.getOpdId()); // RX ID shown as subtitle
        holder.tvTime.setText(item.getTime());

        com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(context);
        String role = sessionManager.getRole();

        // Status Badge
        String status = item.getStatus();
        if ("Issued".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status) || "Created".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText("PENDING");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_light_orange);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#D97706"));
        } else if ("Dispensed".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setText("DISPENSED");
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_light_green);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#059669"));
        } else {
            holder.tvStatusBadge.setText(status.toUpperCase());
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_light_grey);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#64748B"));
        }

        // Item click: Pending → PrescriptionDetail (Mark as Dispensed), Dispensed → PatientMedicationHistoryActivity
        holder.itemView.setOnClickListener(v -> {
            String currentStatus = item.getStatus();
            if ("pharmacist".equalsIgnoreCase(role)) {
                if ("Dispensed".equalsIgnoreCase(currentStatus)) {
                    // Navigate to Patient Medication History for already dispensed records
                    com.simats.frontend.models.DoctorPatient dp = new com.simats.frontend.models.DoctorPatient(
                        item.getPatientId(), 
                        item.getName(), 
                        item.getInitials(), 
                        item.getOpdId().replace("RX-", "#PX-"), // UI display prefix
                        "28 yrs • Male", // Use consistent mock for age/gender if real data is missing
                        "Today", 
                        item.getPhone(), 
                        item.getEmail(), 
                        0, 0, false
                    );
                    android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.PatientMedicationHistoryActivity.class);
                    intent.putExtra("patient_data", dp);
                    context.startActivity(intent);
                    return;
                }
                // Pending: open Prescription Detail with dispense button
                String prescId = item.getOpdId().replace("RX-", "");
                android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.PrescriptionDetailActivity.class);
                intent.putExtra("prescription_id", prescId);
                context.startActivity(intent);
                return;
            }
            if ("doctor".equalsIgnoreCase(role)) {
                // No navigation for doctors in history view
                return;
            }
            // Default for other roles
            android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.PrescriptionDetailActivity.class);
            intent.putExtra("prescription_id", item.getOpdId().replace("RX-", ""));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitials, tvPatientName, tvStatusBadge, tvSubtitle, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
