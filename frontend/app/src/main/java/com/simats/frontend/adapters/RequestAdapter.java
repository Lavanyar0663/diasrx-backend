package com.simats.frontend.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.models.AccessRequest;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private final Context context;
    private final List<AccessRequest> requestList;


    public interface OnActionListener {
        void onApprove(AccessRequest request);
        void onReject(AccessRequest request);
        void onReconsider(AccessRequest request);
        void onItemClick(AccessRequest request);
    }

    private final OnActionListener actionListener;

    public RequestAdapter(Context context, List<AccessRequest> requestList, OnActionListener actionListener) {
        this.context = context;
        this.requestList = requestList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_access_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        AccessRequest request = requestList.get(position);

        holder.tvName.setText(request.getName());
        holder.tvRoleBadge.setText(request.getRole().toUpperCase());
        holder.ivAvatar.setImageResource(request.getAvatarResId() != 0 ? request.getAvatarResId() : R.drawable.ic_logo);

        // Reset visibility
        holder.llPendingActions.setVisibility(View.GONE);
        holder.llStatusActions.setVisibility(View.GONE);
        holder.btnViewProfileItem.setVisibility(View.GONE);
        holder.btnViewReasonItem.setVisibility(View.GONE);

        // Handle UI based on status
        String status = request.getStatus().toUpperCase();

        if (status.equals("PENDING")) {
            holder.llPendingActions.setVisibility(View.VISIBLE);
            
            if (request.getRole().equalsIgnoreCase("Pharmacist")) {
                holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_teal);
                holder.tvRoleBadge.setTextColor(Color.parseColor("#00897B"));
            } else {
                holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_blue);
                holder.tvRoleBadge.setTextColor(Color.parseColor("#0052CC"));
            }
        } else if (status.equals("APPROVED")) {
            holder.llStatusActions.setVisibility(View.VISIBLE);
            holder.btnViewProfileItem.setVisibility(View.VISIBLE);
            holder.tvRoleBadge.setText("APPROVED");
            holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_teal);
            holder.tvRoleBadge.setTextColor(Color.parseColor("#00897B"));
        } else if (status.equals("REJECTED")) {
            holder.llStatusActions.setVisibility(View.VISIBLE);
            holder.btnViewReasonItem.setVisibility(View.VISIBLE);
            holder.tvRoleBadge.setText("REJECTED");
            holder.tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_reject);
            holder.tvRoleBadge.setTextColor(Color.parseColor("#D32F2F"));
        }

        holder.tvTime.setText("• " + request.getTimeAgo());

        // Action Buttons
        holder.btnApprove.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onApprove(request);
        });
 
        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onReject(request);
        });
 
        holder.btnViewProfileItem.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onItemClick(request);
        });

        holder.btnViewReasonItem.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context,
                    com.simats.frontend.RejectionReasonActivity.class);
            intent.putExtra("applicant_name", request.getName());
            intent.putExtra("role", request.getRole());
            context.startActivity(intent);
        });

        holder.ivAvatar.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(request);
            }
        });

        // Click the whole item to view profile
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRoleBadge, tvTime, btnShowProfile;
        LinearLayout llStatusActions, llPendingActions;
        com.google.android.material.button.MaterialButton btnReject, btnApprove, btnViewProfileItem, btnViewReasonItem;
        android.widget.ImageView ivAvatar;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnShowProfile = itemView.findViewById(R.id.btnShowProfile);
            llStatusActions = itemView.findViewById(R.id.llStatusActions);
            llPendingActions = itemView.findViewById(R.id.llPendingActions);
            btnViewProfileItem = itemView.findViewById(R.id.btnViewProfileItem);
            btnViewReasonItem = itemView.findViewById(R.id.btnViewReasonItem);
        }
    }
}
