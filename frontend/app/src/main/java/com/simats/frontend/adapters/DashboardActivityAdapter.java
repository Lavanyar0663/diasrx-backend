package com.simats.frontend.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.models.DashboardActivityItem;

import java.util.List;

public class DashboardActivityAdapter extends RecyclerView.Adapter<DashboardActivityAdapter.ViewHolder> {

    private final Context context;
    private final List<DashboardActivityItem> itemList;

    public DashboardActivityAdapter(Context context, List<DashboardActivityItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardActivityItem item = itemList.get(position);
        com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(context);

        holder.tvTitle.setText(item.getTitle());

        if (item.getType() == DashboardActivityItem.Type.PRESCRIPTION) {
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(item.getSubtitle());
            holder.tvDescription.setVisibility(View.GONE);

            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            holder.tvStatusBadge.setText(item.getStatus());

            holder.tvAvatarInitials.setVisibility(View.VISIBLE);
            holder.tvAvatarInitials.setText(item.getInitials());
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(0); // No vector icon
            holder.ivIcon.setBackgroundResource(R.drawable.bg_circle_grey);
            holder.ivIcon.setColorFilter(null);
            holder.ivIcon.setPadding(0, 0, 0, 0);

            if ("Dispensed".equalsIgnoreCase(item.getStatus()) || "DISPENSED".equalsIgnoreCase(item.getStatus())) {
                holder.tvStatusBadge.setText("DISPENSED");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_light_green);
                holder.tvStatusBadge.setTextColor(Color.parseColor("#059669"));
            } else {
                // Pending / Issued
                holder.tvStatusBadge.setText("PENDING");
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_light_orange);
                holder.tvStatusBadge.setTextColor(Color.parseColor("#D97706"));
            }

            // Click listener for navigation to detail
            holder.itemView.setOnClickListener(v -> {
                try {
                    String role = sessionManager.getRole();
                    String statusVal = item.getStatus() != null ? item.getStatus() : "";
                    String sub = item.getSubtitle() != null ? item.getSubtitle() : "";

                    // Safe parsing of subtitle (e.g., "RX-48 • 10:42")
                    String rxId = sub.contains(" • ") ? sub.split(" • ")[0] : sub;
                    String prescId = rxId.replace("RX-", "").trim();

                    if ("doctor".equalsIgnoreCase(role)) {
                        // No navigation for doctors on dashboard
                        return;
                    }

                    // Pharmacist: Pending → open PrescriptionDetail; Dispensed → no-op
                    if ("pharmacist".equalsIgnoreCase(role)) {
                        if ("Dispensed".equalsIgnoreCase(statusVal)) return;
                        android.content.Intent intent = new android.content.Intent(context,
                                com.simats.frontend.PrescriptionDetailActivity.class);
                        intent.putExtra("prescription_id", prescId);
                        context.startActivity(intent);
                        return;
                    }

                    // Default
                    android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.PrescriptionDetailActivity.class);
                    intent.putExtra("prescription_id", prescId);
                    context.startActivity(intent);
                } catch (Throwable t) {
                    t.printStackTrace();
                    android.util.Log.e("DashboardAdapter", "Navigation Error: " + t.getMessage());
                }
            });

        } else if (item.getType() == DashboardActivityItem.Type.EVENT) {
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(item.getPatientName());
            holder.tvDescription.setVisibility(View.GONE);

            holder.tvStatusBadge.setVisibility(View.VISIBLE);
            holder.tvStatusBadge.setText(item.getTime());
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_grey);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#64748B"));

            holder.tvAvatarInitials.setVisibility(View.GONE);
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(item.getIconResId());
            holder.ivIcon.setBackgroundResource(R.drawable.bg_circle_cyan_light);
            holder.ivIcon.setPadding(20, 20, 20, 20);
            holder.ivIcon.setColorFilter(Color.parseColor("#0D9488"));

        } else {
            // Alert / Info
            holder.tvSubtitle.setVisibility(View.GONE);
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(item.getDescription());
            holder.tvStatusBadge.setVisibility(View.GONE);

            holder.tvAvatarInitials.setVisibility(View.GONE);
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setBackgroundResource(0);
            holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_info);
            holder.ivIcon.setColorFilter(Color.parseColor("#1A237E")); // Dark blue info
            holder.ivIcon.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvAvatarInitials, tvTitle, tvSubtitle, tvDescription, tvStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
