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
import com.simats.frontend.models.Pharmacist;

import java.util.ArrayList;
import java.util.List;

public class PharmacistAdapter extends RecyclerView.Adapter<PharmacistAdapter.PharmacistViewHolder> implements Filterable {

    private final Context context;
    private final List<Pharmacist> pharmacistList;
    private List<Pharmacist> pharmacistListFull;

    public PharmacistAdapter(Context context, List<Pharmacist> pharmacistList) {
        this.context = context;
        this.pharmacistList = pharmacistList;
        this.pharmacistListFull = new ArrayList<>(pharmacistList);
    }

    @NonNull
    @Override
    public PharmacistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_pharmacist, parent, false);
        return new PharmacistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacistViewHolder holder, int position) {
        Pharmacist pharmacist = pharmacistList.get(position);

        holder.tvName.setText(pharmacist.getName());
        holder.tvStatusBadge.setText(pharmacist.getStatus().toUpperCase());

        if (pharmacist.getStatus().equalsIgnoreCase("ACTIVE")) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_teal);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#00897B"));
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_grey);
            holder.tvStatusBadge.setTextColor(Color.parseColor("#5A6B82"));
        }
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.PharmacistDetailsActivity.class);
            intent.putExtra("pharmacist", pharmacist);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pharmacistList.size();
    }

    @Override
    public Filter getFilter() {
        return pharmacistFilter;
    }

    private Filter pharmacistFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Pharmacist> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(pharmacistListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Pharmacist item : pharmacistListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
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
            pharmacistList.clear();
            pharmacistList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class PharmacistViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatusBadge;

        public PharmacistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}
