package com.simats.frontend;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.simats.frontend.adapters.DashboardActivityAdapter;
import com.simats.frontend.databinding.ActivityActivityTimelineBinding;
import com.simats.frontend.models.DashboardActivityItem;
import java.util.ArrayList;
import java.util.List;

public class ActivityTimelineActivity extends AppCompatActivity {

    private ActivityActivityTimelineBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActivityTimelineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> finish());

        setupTimeline();
    }

    private void setupTimeline() {
        binding.rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        
        List<DashboardActivityItem> items = new ArrayList<>();
        // Mock timeline data matching the dashboard but more detailed
        items.add(new DashboardActivityItem("Prescription Dispensed", "Sarah Jenkins", "2m ago", R.drawable.ic_check_cyan_bold));
        items.add(new DashboardActivityItem("Prescription Issued", "Michael Ross", "1h ago", android.R.drawable.ic_menu_add));
        items.add(new DashboardActivityItem("Consultation Created", "Anita Lee", "4h ago", android.R.drawable.ic_menu_edit));
        items.add(new DashboardActivityItem("Prescription Dispensed", "John Doe", "Yesterday", R.drawable.ic_check_cyan_bold));
        items.add(new DashboardActivityItem("Prescription Issued", "Elena Kravtsov", "Yesterday", android.R.drawable.ic_menu_add));
        items.add(new DashboardActivityItem("Consultation Created", "Robert Miller", "Oct 20", android.R.drawable.ic_menu_edit));
        items.add(new DashboardActivityItem("System Update", "DIAS Rx Security", "Oct 19", android.R.drawable.ic_lock_idle_lock));
        items.add(new DashboardActivityItem("Prescription Issued", "James Wilson", "Oct 18", android.R.drawable.ic_menu_add));

        DashboardActivityAdapter adapter = new DashboardActivityAdapter(this, items);
        binding.rvTimeline.setAdapter(adapter);
    }
}
