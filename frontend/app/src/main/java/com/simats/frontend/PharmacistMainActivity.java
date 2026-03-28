package com.simats.frontend;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.simats.frontend.databinding.ActivityPharmacistMainBinding;
import com.simats.frontend.fragments.PharmacistHomeFragment;
import com.simats.frontend.fragments.PharmacistNotificationsFragment;
import com.simats.frontend.fragments.PharmacistPatientsFragment;

public class PharmacistMainActivity extends AppCompatActivity {

    private ActivityPharmacistMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPharmacistMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Default Fragment
        if (savedInstanceState == null) {
            loadFragment(new PharmacistHomeFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                fragment = new PharmacistHomeFragment();
            } else if (itemId == R.id.nav_history) {
                // Now using a fragment-based approach for consistency if needed, 
                // but usually history is an activity. For now, let's keep it consistent with the menu.
                // If the user wants a separate screen, we can navigate to activity.
                fragment = new PharmacistPatientsFragment(); // Assuming this is reused or renamed
            } else if (itemId == R.id.nav_alerts) {
                fragment = new PharmacistNotificationsFragment();
            }

            return loadFragment(fragment);
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            private long backPressedTime;
            @Override
            public void handleOnBackPressed() {
                if (binding.bottomNavigation.getSelectedItemId() != R.id.nav_home) {
                    binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                    return;
                }
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finish();
                    return;
                } else {
                    android.widget.Toast.makeText(PharmacistMainActivity.this, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });
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
