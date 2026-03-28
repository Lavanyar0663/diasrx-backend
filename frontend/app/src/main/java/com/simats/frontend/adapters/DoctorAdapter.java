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
import com.simats.frontend.models.Doctor;

import java.util.ArrayList;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> implements Filterable {

    private final Context context;
    private final List<Doctor> doctorList;
    private List<Doctor> doctorListFull;

    public DoctorAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
        this.doctorListFull = new ArrayList<>(doctorList);
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_doctor, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        holder.tvName.setText(doctor.getName());
        holder.tvDepartment.setText(doctor.getDepartment());
        holder.tvStatusBadge.setText(doctor.getStatus().toUpperCase());

        if (doctor.getStatus().equalsIgnoreCase("ACTIVE")) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#00897B"));
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_grey);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#5A6B82")); // colorTextSecondary roughly
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.DoctorDetailsActivity.class);
            intent.putExtra("doctor", doctor);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    @Override
    public Filter getFilter() {
        return doctorFilter;
    }

    private Filter doctorFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Doctor> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(doctorListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Doctor item : doctorListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || 
                        item.getDepartment().toLowerCase().contains(filterPattern)) {
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
            doctorList.clear();
            doctorList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDepartment, tvStatusBadge;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
