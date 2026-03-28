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
import com.simats.frontend.models.DoctorNotification;

import java.util.List;

public class DoctorNotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<DoctorNotification> notificationList;

    public DoctorNotificationAdapter(Context context, List<DoctorNotification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @Override
    public int getItemViewType(int position) {
        return notificationList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DoctorNotification.TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notification_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_doctor_notification, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DoctorNotification notif = notificationList.get(position);

        if (holder.getItemViewType() == DoctorNotification.TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeader.setText(notif.getHeaderTitle());
        } else {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.tvNotifTitle.setText(notif.getTitle());
            itemHolder.tvNotifDesc.setText(notif.getDescription());
            itemHolder.tvTime.setText(notif.getTime());

            itemHolder.tvTime.setTextColor(context.getResources().getColor(notif.getTimeTextColor()));

            itemHolder.ivNotifIcon.setImageResource(notif.getIconResId());
            itemHolder.ivNotifIcon.setBackgroundResource(notif.getIconBgResId());
            itemHolder.ivNotifIcon.setColorFilter(context.getResources().getColor(notif.getIconTintColor()));

            if (notif.isUnread()) {
                itemHolder.ivUnreadDot.setVisibility(View.VISIBLE);
                itemHolder.tvNotifTitle.setTextColor(Color.parseColor("#1A237E")); // darker for unread
                itemHolder.tvNotifTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                itemHolder.ivUnreadDot.setVisibility(View.GONE);
                itemHolder.tvNotifTitle.setTextColor(Color.parseColor("#455A64")); // normal
                itemHolder.tvNotifTitle.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            itemHolder.itemView.setOnClickListener(v -> {
                if (notif.isUnread()) {
                    notif.setUnread(false);
                    notifyItemChanged(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNotifIcon, ivUnreadDot;
        TextView tvNotifTitle, tvNotifDesc, tvTime;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNotifIcon = itemView.findViewById(R.id.ivNotifIcon);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
            tvNotifTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvNotifDesc = itemView.findViewById(R.id.tvNotifDesc);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
