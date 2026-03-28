package com.simats.frontend.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.models.Medication;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    private final Context context;
    private final List<Medication> medicationList;
    private OnDeleteListener deleteListener;

    public MedicationAdapter(Context context, List<Medication> medicationList) {
        this.context = context;
        this.medicationList = medicationList;
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medication med = medicationList.get(position);
        holder.tvDrugName.setText(med.getName());
        holder.tvQty.setText("Qty: " + med.getQuantity());
        String strengthFreq = (med.getStrength() != null ? med.getStrength() : "")
                + " - " + med.getFrequency()
                + (med.getDuration() != null && !med.getDuration().isEmpty() ? " (" + med.getDuration() + ")" : "");
        holder.tvStrengthFreq.setText(strengthFreq);
        holder.tvInstructions.setText(med.getInstructions());

        holder.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDrugName, tvQty, tvStrengthFreq, tvInstructions;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDrugName = itemView.findViewById(R.id.tvDrugName);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvStrengthFreq = itemView.findViewById(R.id.tvStrengthFreq);
            tvInstructions = itemView.findViewById(R.id.tvInstructions);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
