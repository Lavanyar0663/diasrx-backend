package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simats.frontend.databinding.ActivityDispenseSuccessBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DispenseSuccessActivity extends AppCompatActivity {

    private ActivityDispenseSuccessBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDispenseSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String patientName = getIntent().getStringExtra("patient_name");
        String pId = getIntent().getStringExtra("prescription_id");

        binding.tvSuccessPatient.setText(patientName != null ? patientName : "John Doe");
        
        String idText = pId != null ? (pId.startsWith("RX-") ? pId : "#RX-" + pId) : "#RX-8842-D";
        binding.tvDispenseDesc.setText("The medication for " + patientName + " (" + idText + ") has been successfully marked as dispensed. The Doctor has been notified.");

        binding.tvSuccessStatus.setText("✓ Completed");
        binding.tvSuccessStatus.setTextColor(android.graphics.Color.parseColor("#00BFA5"));

        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        binding.tvSuccessTime.setText(currentTime);

        String txId = "#TXN-" + new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date()) + "-" + (1000 + (int)(Math.random() * 9000));
        binding.tvSuccessTxId.setText(txId);

        binding.btnBackToHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrescriptionHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
