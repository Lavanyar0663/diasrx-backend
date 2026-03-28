package com.simats.frontend;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.simats.frontend.databinding.ActivityDoctorMainBinding;
import com.simats.frontend.fragments.DoctorDashboardFragment;
import com.simats.frontend.fragments.DoctorNotificationsFragment;
import com.simats.frontend.fragments.DoctorPatientsFragment;

public class DoctorMainActivity extends AppCompatActivity {

    private ActivityDoctorMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Default Fragment
        if (savedInstanceState == null) {
            loadFragment(new DoctorDashboardFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                fragment = new DoctorDashboardFragment();
            } else if (itemId == R.id.nav_patients) {
                fragment = new DoctorPatientsFragment();
            } else if (itemId == R.id.nav_notifications) {
                fragment = new DoctorNotificationsFragment();
            }

            return loadFragment(fragment);
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            private long backPressedTime;
            @Override
            public void handleOnBackPressed() {
                if (binding.bottomNavigation.getSelectedItemId() != R.id.nav_dashboard) {
                    binding.bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
                    loadFragment(new com.simats.frontend.fragments.DoctorDashboardFragment());
                    return;
                }
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finishAffinity();
                } else {
                    android.widget.Toast.makeText(DoctorMainActivity.this, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
    }


    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(android.content.Intent intent) {
        if (intent != null && "dashboard".equals(intent.getStringExtra("nav_target"))) {
            binding.bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
            loadFragment(new DoctorDashboardFragment());
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
