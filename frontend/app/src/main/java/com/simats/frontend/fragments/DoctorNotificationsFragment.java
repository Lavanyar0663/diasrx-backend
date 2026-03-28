package com.simats.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.simats.frontend.databinding.FragmentDoctorNotificationsBinding;

public class DoctorNotificationsFragment extends Fragment {

    private FragmentDoctorNotificationsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorNotificationsBinding.inflate(inflater, container, false);

        setupNotificationsList();

        binding.ivBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        getActivity().findViewById(com.simats.frontend.R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(com.simats.frontend.R.id.nav_dashboard);
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(com.simats.frontend.R.id.fragment_container, new DoctorDashboardFragment())
                            .commit();
                }
            }
        });

        binding.tvClear.setOnClickListener(v -> {
            for (com.simats.frontend.models.DoctorNotification notif : list) {
                notif.setUnread(false);
            }
            if (binding.rvNotifications.getAdapter() != null) {
                binding.rvNotifications.getAdapter().notifyDataSetChanged();
            }
            android.widget.Toast.makeText(getContext(), "All caught up!", android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.ivDoctorAvatar.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getContext(),
                    com.simats.frontend.DoctorSettingsActivity.class);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    java.util.List<com.simats.frontend.models.DoctorNotification> list = new java.util.ArrayList<>();

    private void setupNotificationsList() {
        binding.rvNotifications.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        // TODAY
        list.add(new com.simats.frontend.models.DoctorNotification("TODAY"));
        list.add(new com.simats.frontend.models.DoctorNotification(
                "Prescription Dispensed",
                "Prescription for Sarah Jenkins (OPD #8921) has been fully dispensed by the pharmacy.",
                "2m ago", true,
                android.R.drawable.ic_menu_add, com.simats.frontend.R.drawable.bg_badge_teal,
                android.R.color.holo_green_dark, android.R.color.holo_green_dark));
        list.add(new com.simats.frontend.models.DoctorNotification(
                "Consultation Created",
                "New consultation session started for patient Anita Lee (OPD #4421).",
                "15m ago", true,
                android.R.drawable.ic_menu_edit, com.simats.frontend.R.drawable.bg_badge_light_blue,
                android.R.color.holo_blue_dark, android.R.color.holo_blue_dark));
        list.add(new com.simats.frontend.models.DoctorNotification(
                "Lab Results Available",
                "Blood work results for Michael Ross are now available for review.",
                "1h ago", true,
                android.R.drawable.ic_menu_report_image, com.simats.frontend.R.drawable.bg_badge_blue,
                android.R.color.holo_blue_dark, android.R.color.holo_green_dark));
        list.add(new com.simats.frontend.models.DoctorNotification(
                "Appointment Confirmed",
                "Follow-up appointment for Anita Lee confirmed for tomorrow at 10:00 AM.",
                "4h ago", false,
                android.R.drawable.ic_menu_my_calendar, com.simats.frontend.R.drawable.bg_badge_grey,
                android.R.color.darker_gray, android.R.color.darker_gray));

        // YESTERDAY
        list.add(new com.simats.frontend.models.DoctorNotification("YESTERDAY"));
        list.add(new com.simats.frontend.models.DoctorNotification(
                "Drug Interaction Alert",
                "Potential interaction detected for patient John Doe between Amoxicillin and Warfarin.",
                "Yesterday", false,
                android.R.drawable.ic_dialog_alert, com.simats.frontend.R.drawable.bg_badge_light_orange,
                android.R.color.holo_orange_dark, android.R.color.darker_gray));
        list.add(new com.simats.frontend.models.DoctorNotification(
                "Treatment Plan Updated",
                "Dr. Smith updated the root canal treatment plan for Elena Kravtsov.",
                "Yesterday", false,
                android.R.drawable.ic_menu_edit, com.simats.frontend.R.drawable.bg_badge_grey,
                android.R.color.holo_purple, android.R.color.darker_gray));

        // EARLIER
        list.add(new com.simats.frontend.models.DoctorNotification("EARLIER"));
        list.add(new com.simats.frontend.models.DoctorNotification(
                "System Update",
                "DIAS Rx has been successfully updated to version 2.4.0 with new inventory features.",
                "Oct 20", false,
                android.R.drawable.checkbox_on_background, com.simats.frontend.R.drawable.bg_badge_light_green,
                android.R.color.holo_green_dark, android.R.color.darker_gray));

        com.simats.frontend.adapters.DoctorNotificationAdapter adapter = new com.simats.frontend.adapters.DoctorNotificationAdapter(
                getContext(), list);
        binding.rvNotifications.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
