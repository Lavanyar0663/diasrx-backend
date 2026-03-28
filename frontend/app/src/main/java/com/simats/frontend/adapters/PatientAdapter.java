package com.simats.frontend.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> implements Filterable {

    private final Context context;
    private final List<Patient> patientList;
    private List<Patient> patientListFull;

    public PatientAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
        this.patientListFull = new ArrayList<>(patientList);
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient p = patientList.get(position);

        holder.tvName.setText(p.getName());
        holder.tvPid.setText(p.getPid());
        holder.tvAgeGender.setText(p.getAgeGender());
        holder.tvLastVisit.setText("Last Visit: " + p.getLastVisit());
        holder.tvDepartmentBadge.setText(p.getDepartmentBadge());

        // Dynamic badge color based on department text (mockup approximation)
        String dept = p.getDepartmentBadge().toLowerCase();
        if (dept.contains("ortho")) {
            holder.tvDepartmentBadge.setBackgroundResource(R.drawable.bg_badge_light_blue);
            // wait, layout is incorrect, I mean drawable. fallback fixed below
            holder.tvDepartmentBadge.setBackgroundResource(R.drawable.bg_badge_light_blue);
            holder.tvDepartmentBadge.setTextColor(Color.parseColor("#1565C0"));
        } else if (dept.contains("perio")) {
            holder.tvDepartmentBadge.setBackgroundResource(R.drawable.bg_badge_light_purple);
            holder.tvDepartmentBadge.setTextColor(Color.parseColor("#6A1B9A"));
        } else if (dept.contains("surgery")) {
            holder.tvDepartmentBadge.setBackgroundResource(R.drawable.bg_badge_light_orange);
            holder.tvDepartmentBadge.setTextColor(Color.parseColor("#E65100"));
        } else {
            // General Dental (Teal)
            holder.tvDepartmentBadge.setBackgroundResource(R.drawable.bg_badge_teal);
            holder.tvDepartmentBadge.setTextColor(Color.parseColor("#00897B"));
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.PatientDetailsActivity.class);
            intent.putExtra("patient", p);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    @Override
    public Filter getFilter() {
        return patientFilter;
    }

    private Filter patientFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Patient> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(patientListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Patient item : patientListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || 
                        item.getDepartmentBadge().toLowerCase().contains(filterPattern) ||
                        item.getPid().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            patientList.clear();
            patientList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDepartmentBadge, tvPid, tvAgeGender, tvLastVisit;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDepartmentBadge = itemView.findViewById(R.id.tvDepartmentBadge);
            tvPid = itemView.findViewById(R.id.tvPid);
            tvAgeGender = itemView.findViewById(R.id.tvAgeGender);
            tvLastVisit = itemView.findViewById(R.id.tvLastVisit);
        }
    }
}
