package com.simats.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.adapters.PharmacistNotificationAdapter;
import com.simats.frontend.models.DoctorNotification;

import java.util.ArrayList;
import java.util.List;

public class PharmacistNotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pharmacist_notifications, container, false);

        RecyclerView rvNotifications = view.findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        List<DoctorNotification> notifications = new ArrayList<>();
        notifications.add(new DoctorNotification("TODAY"));
        
        // Stat Insulin Order - Red/Orange
        notifications.add(new DoctorNotification("Stat Insulin Order", "Bed 402, Ward B requires immediate validation.",
                "Just now", true, android.R.drawable.stat_sys_warning, R.drawable.bg_badge_light_orange,
                android.R.color.holo_red_dark, R.color.colorTextSecondary));
        
        // Patient: Sarah Jenkins - Light Blue/Cyan
        notifications.add(new DoctorNotification("Patient: Sarah Jenkins", "Prescribed by Dr. Emily Chen", "4m ago",
                true, android.R.drawable.ic_menu_edit, R.drawable.bg_badge_light_blue, R.color.colorPrimary,
                R.color.colorTextSecondary));
        
        // Consultation Created - Light Blue/Cyan
        notifications.add(new DoctorNotification("Consultation Created",
                "New consultation started for patient Anita Lee (OPD #4421).",
                "15m ago", true, android.R.drawable.ic_menu_edit, R.drawable.bg_badge_light_blue, R.color.colorPrimary,
                R.color.colorTextSecondary));
        
        // Access Approved - Dark Blue/Indigo
        notifications.add(new DoctorNotification("Access Approved",
                "Your access to the High-Risk Medication Vault has been granted.", "2h ago", false,
                android.R.drawable.ic_secure, R.drawable.bg_badge_light_blue, R.color.colorSecondary,
                R.color.colorTextSecondary));
        
        // Patient: Robert Miller - Grey/Blue
        notifications.add(new DoctorNotification("Patient: Robert Miller", "Prescribed by Dr. Michael Ross", "5h ago",
                false, android.R.drawable.ic_menu_edit, R.drawable.bg_badge_light_grey, R.color.colorTextSecondary,
                R.color.colorTextSecondary));

        notifications.add(new DoctorNotification("YESTERDAY"));
        notifications.add(new DoctorNotification("Low Stock: Amoxicillin",
                "Main Pharmacy stock is below 20%. Order replenishment soon.", "Yesterday", false,
                android.R.drawable.ic_menu_agenda, R.drawable.bg_badge_light_grey, R.color.colorTextSecondary,
                R.color.colorTextSecondary));

        PharmacistNotificationAdapter adapter = new PharmacistNotificationAdapter(getContext(), notifications);
        rvNotifications.setAdapter(adapter);

        view.findViewById(R.id.tvMarkAllRead).setOnClickListener(v -> {
            for (DoctorNotification notif : notifications) {
                notif.setUnread(false);
            }
            adapter.notifyDataSetChanged();
            showCustomSuccessSnackbar(view, "All caught up!");
        });

        // Profile icon click
        android.view.View cvProfile = view.findViewById(R.id.cvProfile);
        android.view.View ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        android.view.View.OnClickListener profileClickListener = v -> {
            startActivity(new android.content.Intent(getContext(), com.simats.frontend.PharmacistProfileActivity.class));
        };
        if (cvProfile != null) cvProfile.setOnClickListener(profileClickListener);
        if (ivProfileAvatar != null) ivProfileAvatar.setOnClickListener(profileClickListener);

        return view;
    }

    private void showCustomSuccessSnackbar(View parentView, String message) {
        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(
                parentView, "", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);
        
        com.google.android.material.snackbar.Snackbar.SnackbarLayout snackLayout = (com.google.android.material.snackbar.Snackbar.SnackbarLayout) snackbar.getView();
        snackLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        
        View customView = getLayoutInflater().inflate(R.layout.layout_notif_success_toast, null);
        
        // Ensure the width is set to wrap_content and it's centered
        android.view.View view = snackLayout;
        android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = android.widget.FrameLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = (int) (80 * getResources().getDisplayMetrics().density); // Position it above the bottom nav
        view.setLayoutParams(params);
        
        snackLayout.addView(customView, 0);
        snackbar.show();
    }
}
