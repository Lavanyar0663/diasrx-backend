package com.simats.frontend.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.frontend.R;
import com.simats.frontend.models.MedicationHistoryEntry;

import java.util.List;

public class MedicationHistoryAdapter extends RecyclerView.Adapter<MedicationHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<MedicationHistoryEntry> entries;
    private final com.simats.frontend.models.DoctorPatient patientData;

    public MedicationHistoryAdapter(Context context, List<MedicationHistoryEntry> entries, com.simats.frontend.models.DoctorPatient patientData) {
        this.context = context;
        this.entries = entries;
        this.patientData = patientData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            MedicationHistoryEntry entry = entries.get(position);



            holder.tvDate.setText(entry.getDate().toUpperCase());
            holder.tvTime.setText(entry.getTime());
            holder.tvVisitTitle.setText(entry.getDepartment());
            holder.tvDiagnosis.setText(entry.getDiagnosis());

            holder.llDrugs.removeAllViews();

            if (entry.getDrugs() == null || entry.getDrugs().isEmpty()) {
                TextView noMed = new TextView(context);
                noMed.setText("No medication prescribed for this visit.");
                noMed.setTextColor(context.getResources().getColor(R.color.colorTextSecondary));
                noMed.setTextSize(13f);
                noMed.setPadding(0, 8, 0, 0);
                holder.llDrugs.addView(noMed);
            } else {
                for (MedicationHistoryEntry.DrugEntry drug : entry.getDrugs()) {
                    // Main card container
                    androidx.cardview.widget.CardView drugCard = new androidx.cardview.widget.CardView(context);
                    LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    cardLp.setMargins(0, 12, 0, 4);
                    drugCard.setLayoutParams(cardLp);
                    drugCard.setRadius(24f); // 12dp
                    drugCard.setCardElevation(0f);
                    drugCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorBackground));
                    
                    LinearLayout innerLayout = new LinearLayout(context);
                    innerLayout.setOrientation(LinearLayout.VERTICAL);
                    innerLayout.setPadding(24, 24, 24, 24);
                    
                    // Row 1: Icon, Name, Strength
                    LinearLayout row1 = new LinearLayout(context);
                    row1.setOrientation(LinearLayout.HORIZONTAL);
                    row1.setGravity(android.view.Gravity.CENTER_VERTICAL);

                    ImageView ivPill = new ImageView(context);
                    ivPill.setImageResource(R.drawable.ic_pill);
                    ivPill.setLayoutParams(new LinearLayout.LayoutParams(48, 48)); // Ensure visible in px
                    ivPill.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    row1.addView(ivPill);

                    TextView tvName = new TextView(context);
                    tvName.setText((drug.getName().toLowerCase().contains("tab") ? "" : "Tab. ") + drug.getName());
                    tvName.setTextSize(14f);
                    tvName.setTypeface(null, Typeface.BOLD);
                    tvName.setTextColor(context.getResources().getColor(R.color.colorSecondary));
                    LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    nameLp.setMargins(16, 0, 0, 0);
                    tvName.setLayoutParams(nameLp);
                    row1.addView(tvName);

                    TextView tvStrength = new TextView(context);
                    tvStrength.setText(drug.getStrength());
                    tvStrength.setTextSize(12f);
                    tvStrength.setPadding(16, 4, 16, 4);
                    tvStrength.setBackgroundResource(R.drawable.bg_rounded_grey_light);
                    tvStrength.setTextColor(context.getResources().getColor(R.color.colorTextSecondary));
                    row1.addView(tvStrength);

                    innerLayout.addView(row1);

                    // Row 2: Sub-info (dose and duration)
                    TextView tvInfo = new TextView(context);
                    tvInfo.setText(drug.getFrequency() + " for " + drug.getDuration());
                    tvInfo.setTextSize(12f);
                    tvInfo.setTextColor(context.getResources().getColor(R.color.colorTextSecondary));
                    tvInfo.setPadding(48, 4, 0, 0);
                    innerLayout.addView(tvInfo);

                    // Re-issue Prescription Action (Per-drug, as requested)
                    com.simats.frontend.network.SessionManager sessionManager = new com.simats.frontend.network.SessionManager(context);
                    if (!"pharmacist".equalsIgnoreCase(sessionManager.getRole())) {
                        TextView tvAction = new TextView(context);
                        tvAction.setText(position == 0 ? "Re-issue Prescription" : "Copy to New");
                        tvAction.setTextSize(12f);
                        tvAction.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                        tvAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy_content, 0, 0, 0);
                        tvAction.setCompoundDrawablePadding(8);
                        tvAction.setGravity(android.view.Gravity.END);
                        LinearLayout.LayoutParams actionLp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        actionLp.setMargins(0, 12, 0, 0);
                        tvAction.setLayoutParams(actionLp);

                        tvAction.setOnClickListener(v -> {
                            android.content.Intent intent = new android.content.Intent(context, com.simats.frontend.CreatePrescriptionActivity.class);
                            intent.putExtra("patient_data", patientData);
                            
                            // Collect all drugs for this visit to re-issue
                            java.util.ArrayList<com.simats.frontend.models.Medication> medsToReissue = new java.util.ArrayList<>();
                            for (MedicationHistoryEntry.DrugEntry d : entry.getDrugs()) {
                                medsToReissue.add(d.toMedication());
                            }
                            intent.putExtra("reissue_meds", medsToReissue);
                            intent.putExtra("reissue_diagnosis", entry.getDiagnosis());
                            
                            context.startActivity(intent);
                        });
                        innerLayout.addView(tvAction);
                    }

                    drugCard.addView(innerLayout);
                    holder.llDrugs.addView(drugCard);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            android.util.Log.e("MedHistoryAdapter", "Error in onBind: " + t.getMessage());
        }
    }

    private TextView createPillView(String text) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(11f);
        tv.setTextColor(context.getResources().getColor(R.color.colorTextSecondary));
        tv.setBackgroundResource(R.drawable.bg_pill_grey); // Reusing existing pill drawable or generic rounded grey
        tv.setPadding(24, 8, 24, 8);
        return tv;
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvVisitTitle, tvDiagnosis, tvPrescriptionLabel;
        LinearLayout llDrugs;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvVisitTitle = itemView.findViewById(R.id.tvVisitTitle);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
            tvPrescriptionLabel = itemView.findViewById(R.id.tvPrescriptionLabel);
            llDrugs = itemView.findViewById(R.id.llDrugs);
        }
    }
}
