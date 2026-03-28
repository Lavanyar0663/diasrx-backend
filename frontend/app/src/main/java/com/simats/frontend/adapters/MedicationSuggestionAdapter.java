package com.simats.frontend.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.simats.frontend.R;

import java.util.ArrayList;
import java.util.List;

public class MedicationSuggestionAdapter extends ArrayAdapter<JsonObject> {
    private List<JsonObject> fullList;
    private List<JsonObject> mSuggestions;
    private String mQuery = "";

    public MedicationSuggestionAdapter(@NonNull Context context, @NonNull List<JsonObject> drugList) {
        super(context, 0, new ArrayList<>(drugList));
        this.fullList = new ArrayList<>(drugList);
        this.mSuggestions = new ArrayList<>();
    }

    public void updateData(List<JsonObject> newData) {
        this.fullList = new ArrayList<>(newData);
        // We don't call notifyDataSetChanged here because we usually trigger filter() right after
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_medication_suggestion, parent, false);
        }

        JsonObject drug = getItem(position);
        TextView tvName = convertView.findViewById(R.id.tvDrugName);
        TextView tvStrength = convertView.findViewById(R.id.tvDrugStrength);

        if (drug != null) {
            if (drug.has("no_results")) {
                tvName.setText("No results found");
                tvName.setTextColor(Color.GRAY);
                tvStrength.setText("Try a different search term");
                return convertView;
            }
            
            tvName.setTextColor(Color.parseColor("#1E293B"));
            String fullName = drug.has("name") ? drug.get("name").getAsString() : "Unknown Drug";
            
            // Basic extraction of strength if it's in the name (e.g. "Amoxicillin 500mg")
            String namePart = fullName;
            String strengthPart = "Dosage information not available";
            
            // Simple regex/split for strength (common patterns like 500mg, 10ml, etc)
            int firstDigit = -1;
            for(int i=0; i<fullName.length(); i++) {
                if(Character.isDigit(fullName.charAt(i))) {
                    firstDigit = i;
                    break;
                }
            }
            
            if (firstDigit != -1) {
                namePart = fullName.substring(0, firstDigit).trim();
                strengthPart = fullName.substring(firstDigit).trim();
            }

            // Highlight matching text in namePart
            SpannableString spannable = new SpannableString(namePart);
            if (!mQuery.isEmpty()) {
                String normalizedName = namePart.toLowerCase();
                String normalizedQuery = mQuery.toLowerCase();
                int start = normalizedName.indexOf(normalizedQuery);
                if (start != -1) {
                    spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#06B6D4")), start, start + mQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new StyleSpan(Typeface.BOLD), start, start + mQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            
            tvName.setText(spannable);
            tvStrength.setText(strengthPart);
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<JsonObject> suggestions = new ArrayList<>();
                if (constraint != null && constraint.length() > 0) {
                    mQuery = constraint.toString();
                    for (JsonObject drug : fullList) {
                        if (drug.has("name")) {
                            String name = drug.get("name").getAsString().toLowerCase();
                            if (name.contains(constraint.toString().toLowerCase())) {
                                suggestions.add(drug);
                            }
                        }
                    }
                    
                    if (suggestions.isEmpty() && constraint.length() >= 2) {
                        JsonObject noResults = new JsonObject();
                        noResults.addProperty("no_results", true);
                        suggestions.add(noResults);
                    }
                }
                results.values = suggestions;
                results.count = suggestions.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mSuggestions.clear();
                if (results != null && results.count > 0) {
                    mSuggestions.addAll((List<JsonObject>) results.values);
                    clear();
                    addAll(mSuggestions);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                JsonObject json = (JsonObject) resultValue;
                if (json != null && json.has("no_results")) {
                    return "";
                }
                return (json != null && json.has("name")) ? json.get("name").getAsString() : "";
            }
        };
    }
}
