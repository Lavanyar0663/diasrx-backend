package com.simats.frontend;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.frontend.databinding.ActivityPrescriptionSuccessBinding;
import com.simats.frontend.models.Medication;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class PrescriptionSuccessActivity extends AppCompatActivity {

    private ActivityPrescriptionSuccessBinding binding;
    private List<Medication> medicationList;
    private String patientName, opdId, ageGender, phone, email;
    private String diagnosis, department;
    private String aiEngInstructions, aiTamInstructions, aiEngReminders, aiTamReminders;

    private File generatedPdf = null;
    private boolean pdfReady = false;
    // Flag: whether we launched an external app (email/WA) — used to suppress back navigation
    private boolean launchedExternalApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescriptionSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ── Intent data ──────────────────────────────────────────────────────
        patientName       = getIntent().getStringExtra("patient_name");
        opdId             = getIntent().getStringExtra("opd_id");
        ageGender         = getIntent().getStringExtra("age_gender");
        phone             = getIntent().getStringExtra("phone");
        email             = getIntent().getStringExtra("email");  // only if doctor left it in form
        int medCount      = getIntent().getIntExtra("med_count", 0);
        medicationList    = (List<Medication>) getIntent().getSerializableExtra("medication_list");
        diagnosis         = getIntent().getStringExtra("diagnosis");
        department        = getIntent().getStringExtra("department");
        aiEngInstructions = getIntent().getStringExtra("ai_explanation");
        aiTamInstructions = getIntent().getStringExtra("ai_explanation_tamil");
        aiEngReminders    = getIntent().getStringExtra("ai_reminders");
        aiTamReminders    = getIntent().getStringExtra("ai_reminders_tamil");

        // ── Populate UI ──────────────────────────────────────────────────────
        binding.tvPatientName.setText(val(patientName));
        binding.tvOpdId.setText(val(opdId));
        binding.tvAgeSex.setText(val(ageGender));
        binding.tvMobile.setText(val(phone));

        boolean hasEmail = email != null && !email.trim().isEmpty();
        if (hasEmail) {
            binding.tvEmail.setText(email.trim());
            binding.tvEmail.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmail.setVisibility(View.GONE);
            View emailRow = binding.getRoot().findViewWithTag("emailLabelRow");
            if (emailRow != null) emailRow.setVisibility(View.GONE);
        }
        binding.tvMedCount.setText(medCount + (medCount == 1 ? " Item" : " Items"));

        if (medicationList != null && !medicationList.isEmpty()) {
            binding.rvMedicationPreview.setLayoutManager(new LinearLayoutManager(this));
            binding.rvMedicationPreview.setAdapter(new MedAdapter(medicationList));
        }

        // ── Start PDF generation immediately ─────────────────────────────────
        binding.btnSharePrescription.setEnabled(false);
        binding.btnDownloadPdf.setEnabled(false);
        binding.pdfLoadingOverlay.setVisibility(View.VISIBLE);
        generatePdfAsync();

        // ── Listeners ────────────────────────────────────────────────────────
        binding.btnSharePrescription.setOnClickListener(v -> showShareDialog());
        binding.btnDownloadPdf.setOnClickListener(v -> downloadPdf());
        binding.btnViewHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, PrescriptionHistoryActivity.class));
            // Note: do NOT finish() here so user can come back
        });
        binding.btnBackToDashboard.setOnClickListener(v -> goToDashboard());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If we just came back from sharing, we want to stay here
        if (launchedExternalApp) {
            // Optional: show a small reminder or just stay silent
            launchedExternalApp = false; 
        }
    }

    @Override
    public void onBackPressed() {
        // If we are showing a dialog or sharing, we don't want to accidentally finish
        if (launchedExternalApp) {
            launchedExternalApp = false;
            return;
        }
        
        com.simats.frontend.utils.DialogHelper.showConfirmationDialog(
                this,
                R.drawable.ic_logout, // Generic warning
                "Leave screen?",
                "Are you sure you want to go back to the Dashboard?",
                "Dashboard",
                "Stay",
                this::goToDashboard
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        goToDashboard();
        return true;
    }

    private String val(String s) {
        return (s != null && !s.trim().isEmpty()) ? s.trim() : "—";
    }

    // ── PDF generation in background ─────────────────────────────────────────

    private void generatePdfAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            File pdf = buildPdf();
            new Handler(Looper.getMainLooper()).post(() -> {
                binding.pdfLoadingOverlay.setVisibility(View.GONE);
                if (pdf != null) {
                    generatedPdf = pdf;
                    pdfReady = true;
                    binding.btnSharePrescription.setEnabled(true);
                    binding.btnDownloadPdf.setEnabled(true);
                } else {
                    showCustomSnackbar("PDF generation failed. Please try again.");
                }
            });
        });
    }

    private File buildPdf() {
        android.graphics.pdf.PdfDocument doc = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pi =
                new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 1200, 1).create();
        android.graphics.pdf.PdfDocument.Page pg = doc.startPage(pi);
        android.graphics.Canvas cv = pg.getCanvas();

        android.graphics.Paint pBold = new android.graphics.Paint();
        pBold.setFakeBoldText(true); pBold.setTextSize(11);
        pBold.setColor(android.graphics.Color.parseColor("#011936"));

        android.graphics.Paint pNorm = new android.graphics.Paint();
        pNorm.setFakeBoldText(false); pNorm.setTextSize(11);
        pNorm.setColor(android.graphics.Color.parseColor("#546E7A"));

        android.graphics.Paint pTeal = new android.graphics.Paint();
        pTeal.setFakeBoldText(true); pTeal.setTextSize(12);
        pTeal.setColor(android.graphics.Color.parseColor("#00796B"));

        android.graphics.Paint pLine = new android.graphics.Paint();
        pLine.setColor(android.graphics.Color.parseColor("#E0E5EA"));

        android.graphics.Paint pSmall = new android.graphics.Paint();
        pSmall.setTextSize(9);
        pSmall.setColor(android.graphics.Color.parseColor("#90A4AE"));

        String today = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

        // Header
        pTeal.setTextSize(20);
        cv.drawText("DIAS Rx — Medical Prescription", 50, 55, pTeal);
        pSmall.setTextSize(10);
        cv.drawText("Date: " + today, 430, 55, pSmall);
        cv.drawLine(50, 68, 545, 68, pLine);

        // Patient Info
        int y = 90; pTeal.setTextSize(11);
        cv.drawText("PATIENT INFORMATION", 50, y, pTeal); y += 18;
        cv.drawText("Name:", 50, y, pNorm);    cv.drawText(val(patientName), 120, y, pBold);
        cv.drawText("OPD ID:", 310, y, pNorm); cv.drawText(val(opdId), 380, y, pBold); y += 17;
        cv.drawText("Age/Sex:", 50, y, pNorm); cv.drawText(val(ageGender), 120, y, pBold);
        cv.drawText("Mobile:", 310, y, pNorm); cv.drawText(val(phone), 380, y, pBold); y += 17;
        if (email != null && !email.trim().isEmpty()) {
            cv.drawText("Email:", 50, y, pNorm);
            cv.drawText(email.trim(), 120, y, pBold); y += 17;
        }
        y += 4; cv.drawLine(50, y, 545, y, pLine);

        // Clinical
        y += 14; cv.drawText("CLINICAL DETAILS", 50, y, pTeal); y += 18;
        cv.drawText("Diagnosis:", 50, y, pNorm);  cv.drawText(val(diagnosis), 150, y, pBold); y += 17;
        cv.drawText("Department:", 50, y, pNorm); cv.drawText(val(department), 150, y, pBold);
        y += 10; cv.drawLine(50, y, 545, y, pLine);

        // Medicines
        y += 14; cv.drawText("PRESCRIBED MEDICINES", 50, y, pTeal); y += 18;
        if (medicationList != null) {
            int num = 1;
            for (Medication m : medicationList) {
                if (y > 680) break;
                String strength = (m.getStrength() != null && !m.getStrength().isEmpty()) ? " " + m.getStrength() : "";
                cv.drawText(num + ". " + m.getName() + strength, 50, y, pBold);
                pNorm.setTextSize(10);
                cv.drawText("Qty: " + m.getQuantity(), 480, y, pNorm); y += 15;
                cv.drawText("   " + m.getFrequency() + " - " + m.getInstructions(), 50, y, pNorm);
                pNorm.setTextSize(11);
                y += 18; num++;
            }
        }
        y += 4; cv.drawLine(50, y, 545, y, pLine);

        // AI bilingual instructions
        boolean hasAi = (aiEngInstructions != null && !aiEngInstructions.trim().isEmpty())
                     || (aiTamInstructions != null && !aiTamInstructions.trim().isEmpty());
        if (hasAi) {
            y += 14; cv.drawText("AI PATIENT INSTRUCTIONS", 50, y, pTeal); y += 4;
            if (aiEngInstructions != null && !aiEngInstructions.trim().isEmpty()) {
                y += 14; pBold.setTextSize(10);
                cv.drawText("Simplified Instructions (English)", 50, y, pBold);
                pNorm.setTextSize(10); y += 14;
                y = wrapText(cv, aiEngInstructions, pNorm, 50, y, 80);
                pBold.setTextSize(11); pNorm.setTextSize(11);
            }
            if (aiTamInstructions != null && !aiTamInstructions.trim().isEmpty()) {
                y += 8; pBold.setTextSize(10);
                cv.drawText("Simplified Instructions (Tamil)", 50, y, pBold);
                pNorm.setTextSize(10); y += 14;
                y = wrapText(cv, aiTamInstructions, pNorm, 50, y, 80);
                pBold.setTextSize(11); pNorm.setTextSize(11);
            }
            if (aiEngReminders != null && !aiEngReminders.trim().isEmpty()) {
                y += 8; pBold.setTextSize(10);
                cv.drawText("Medication Schedule (English)", 50, y, pBold);
                pNorm.setTextSize(10); y += 14;
                y = wrapText(cv, aiEngReminders, pNorm, 50, y, 80);
                pBold.setTextSize(11); pNorm.setTextSize(11);
            }
            if (aiTamReminders != null && !aiTamReminders.trim().isEmpty()) {
                y += 8; pBold.setTextSize(10);
                cv.drawText("Medication Schedule (Tamil)", 50, y, pBold);
                pNorm.setTextSize(10); y += 14;
                y = wrapText(cv, aiTamReminders, pNorm, 50, y, 80);
            }
        }

        // Footer
        pSmall.setTextSize(9);
        cv.drawLine(50, 1180, 545, 1180, pLine);
        cv.drawText("Generated by DIAS Rx - For official medical use only - " + today, 80, 1195, pSmall);

        doc.finishPage(pg);

        String safeName = "Prescription_" + val(opdId).replaceAll("[^a-zA-Z0-9]", "") + ".pdf";
        File out = new File(getExternalCacheDir(), safeName);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            doc.writeTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
            doc.close();
            return null;
        }
        doc.close();
        return out;
    }

    private int wrapText(android.graphics.Canvas cv, String text,
                          android.graphics.Paint p, int x, int y, int charsPerLine) {
        if (text == null || text.isEmpty()) return y;
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String w : words) {
            if (line.length() + w.length() > charsPerLine) {
                if (y > 1150) break;
                cv.drawText(line.toString().trim(), x, y, p);
                y += 13; line = new StringBuilder();
            }
            line.append(w).append(" ");
        }
        if (line.length() > 0 && y < 1150) { cv.drawText(line.toString().trim(), x, y, p); y += 13; }
        return y;
    }

    // ── Share dialog ──────────────────────────────────────────────────────────

    private void showShareDialog() {
        if (!pdfReady || generatedPdf == null) {
            showCustomSnackbar("PDF is still being prepared. Please wait.");
            return;
        }
        
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.Theme_Frontend);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_share_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        View btnWhatsapp = bottomSheetView.findViewById(R.id.btnShareWhatsApp);
        View btnEmail = bottomSheetView.findViewById(R.id.btnShareEmail);
        TextView tvEmailAddress = bottomSheetView.findViewById(R.id.tvEmailAddress);

        boolean hasEmail = email != null && !email.trim().isEmpty();
        if (hasEmail) {
            btnEmail.setVisibility(View.VISIBLE);
            tvEmailAddress.setText("Email - " + email.trim());
        } else {
            btnEmail.setVisibility(View.GONE);
        }

        btnWhatsapp.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            shareViaWhatsApp();
        });

        btnEmail.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            shareViaEmail();
        });

        // The user specifically requested: "If patient has no email -> show WhatsApp only"
        // In our case, the Bottom Sheet just shows WhatsApp. If we want it to automatically launch WhatsApp when no email exists without showing a bottom sheet, we can do that.
        // But a bottom sheet is better UX.
        // Wait, the prompt says: "If patient has no email -> show WhatsApp only". So showing the dialog with only WhatsApp is correct.
        bottomSheetDialog.show();
    }

    private void shareViaEmail() {
        try {
            if (generatedPdf == null || email == null || email.trim().isEmpty()) {
                showCustomSnackbar("Patient email not available.");
                return;
            }
            // Use hardcoded authority to match manifest
            Uri pdfUri = FileProvider.getUriForFile(this, "com.simats.frontend.fileprovider", generatedPdf);

            // Use ACTION_SENDTO with mailto: to force only email apps to respond
            Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
            selectorIntent.setData(Uri.parse("mailto:"));

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email.trim()});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Prescription - " + val(patientName) + " (" + val(opdId) + ")");
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                    "Dear " + val(patientName) + ",\n\nPlease find your prescription attached.\n"
                    + "OPD: " + val(opdId) + "\n\nRegards,\nDIAS Rx Medical System");
            emailIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            emailIntent.setSelector(selectorIntent); // CRITICAL: Filters for email apps only

            launchedExternalApp = true;
            startActivity(Intent.createChooser(emailIntent, "Send via Email"));
        } catch (Exception ex) {
            ex.printStackTrace();
            launchedExternalApp = false;
            showCustomSnackbar("Failed to open email app: " + ex.getMessage());
        }
    }

    private void goToDashboard() {
        Intent i = new Intent(this, DoctorMainActivity.class);
        i.putExtra("nav_target", "dashboard"); // Explicitly target dashboard
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void shareViaWhatsApp() {
        if (generatedPdf == null || phone == null || phone.trim().isEmpty()) {
            showCustomSnackbar("Required data for sharing is missing.");
            return;
        }

        Uri pdfUri = FileProvider.getUriForFile(this, "com.simats.frontend.fileprovider", generatedPdf);
        String clean = phone.replaceAll("[^\\d]", "");
        if (clean.length() == 10) clean = "91" + clean;
        final String finalClean = clean;

        final String finalMsg = "Hello " + val(patientName) + ", your prescription from DIAS Rx is attached.";

        // OPTIMIZED WORKFLOW: Show instructions first, then open direct chat
        com.simats.frontend.utils.DialogHelper.showConfirmationDialog(
            this,
            R.drawable.ic_logout, // Instructions icon (could be info)
            "WhatsApp Sharing Instructions",
            "We are opening a direct chat with " + val(patientName) + ".\n\n" +
                  "How to attach the prescription:\n" +
                  "1. Tap the Attach (📎) icon in WhatsApp\n" +
                  "2. Select 'Document'\n" +
                  "3. Select the Prescription PDF from the list\n\n" +
                  "The chat will open with a pre-filled message once you click below.",
            "Open WhatsApp",
            "Cancel",
            () -> {
                try {
                    // Try direct wa.me link first for immediate chat opening
                    String url = "https://api.whatsapp.com/send?phone=" + finalClean + "&text=" + android.net.Uri.encode(finalMsg);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setPackage("com.whatsapp");
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        // Try WhatsApp Business package
                        intent.setPackage("com.whatsapp.w4b");
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    try {
                        // Fallback to standard SEND intent if VIEW fails
                        Intent waSend = new Intent(Intent.ACTION_SEND);
                        waSend.setType("application/pdf");
                        waSend.putExtra(Intent.EXTRA_STREAM, pdfUri);
                        waSend.putExtra("jid", finalClean + "@s.whatsapp.net");
                        waSend.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        waSend.setClipData(android.content.ClipData.newRawUri("", pdfUri));
                        
                        try {
                            waSend.setPackage("com.whatsapp");
                            startActivity(waSend);
                        } catch (Exception e2) {
                            waSend.setPackage("com.whatsapp.w4b");
                            startActivity(waSend);
                        }
                    } catch (Exception e3) {
                        showCustomSnackbar("WhatsApp is not installed.");
                    }
                }
            }
        );
        
        launchedExternalApp = true;
    }

    // ── Download ──────────────────────────────────────────────────────────────

    private void downloadPdf() {
        if (!pdfReady || generatedPdf == null) {
            showCustomSnackbar("PDF is still being prepared. Please wait.");
            return;
        }
        String fileName = "Prescription_" + val(opdId).replaceAll("[^a-zA-Z0-9]", "") + ".pdf";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                cv.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(
                        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), cv);
                if (uri != null) {
                    OutputStream os = getContentResolver().openOutputStream(uri);
                    java.io.FileInputStream fis = new java.io.FileInputStream(generatedPdf);
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = fis.read(buf)) > 0) os.write(buf, 0, n);
                    os.close();
                    fis.close();
                    showMinimalSuccessPopup("Saved to Downloads ✅");
                } else {
                    showCustomSnackbar("Could not save file. Please check storage.");
                }
            } else {
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File dest = new File(downloads, fileName);
                try (java.io.FileInputStream fis = new java.io.FileInputStream(generatedPdf);
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = fis.read(buf)) > 0) fos.write(buf, 0, n);
                    showMinimalSuccessPopup("Saved to Downloads ✅");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showCustomSnackbar("Save failed: " + e.getMessage());
        }
    }

    private void showMinimalSuccessPopup(String message) {
        // Custom DIAS Rx minimal popup at bottom
        View layout = getLayoutInflater().inflate(R.layout.layout_minimal_success, null);
        TextView tv = layout.findViewById(R.id.tvMessage);
        tv.setText(message);

        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), "", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT);
        com.google.android.material.snackbar.Snackbar.SnackbarLayout snackLayout = (com.google.android.material.snackbar.Snackbar.SnackbarLayout) snackbar.getView();
        snackLayout.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        snackLayout.setPadding(0, 0, 0, 80); // bottom margin
        snackLayout.addView(layout, 0);
        
        // Adjust gravity to bottom center
        android.view.View view = snackLayout;
        android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = android.widget.FrameLayout.LayoutParams.WRAP_CONTENT;
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        view.setLayoutParams(params);
        
        snackbar.show();
    }

    // ── Medicine preview adapter ──────────────────────────────────────────────

    private class MedAdapter extends RecyclerView.Adapter<MedAdapter.VH> {
        final List<Medication> list;
        MedAdapter(List<Medication> list) { this.list = list; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_prescription_preview_drug, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Medication m = list.get(pos);
            String strength = (m.getStrength() != null && !m.getStrength().isEmpty()) ? " " + m.getStrength() : "";
            h.name.setText(m.getName() + strength);
            h.detail.setText(m.getFrequency() + " - " + m.getInstructions());
            h.qty.setText("Qty: " + m.getQuantity());
        }

        @Override public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView name, detail, qty;
            VH(View v) {
                super(v);
                name   = v.findViewById(R.id.tvDrugName);
                detail = v.findViewById(R.id.tvDrugDetails);
                qty    = v.findViewById(R.id.tvDrugQty);
            }
        }
    }

    // ── Custom Teal Snackbar Utility ──────────────────────────────────────────
    private void showCustomSnackbar(String message) {
        // Floating premium Snackbar for ERRORS (High contrast Teal)
        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(
                binding.getRoot(), message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        
        android.view.View snackBarView = snackbar.getView();
        snackBarView.setBackgroundResource(R.drawable.bg_custom_snackbar);
        snackBarView.setBackgroundTintList(null); 
        
        android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) snackBarView.getLayoutParams();
        int marginSides = (int) (48 * getResources().getDisplayMetrics().density);
        int marginBottom = (int) (64 * getResources().getDisplayMetrics().density);
        params.setMargins(marginSides, 0, marginSides, marginBottom);
        
        android.widget.TextView tv = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
        if (tv != null) {
            tv.setTextColor(android.graphics.Color.WHITE);
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        snackbar.show();
    }

    private void showSuccessPopup(String message) {
        // Premium centered success card - a discrete pop up like high-end apps
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_success_popup);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Non-blocking but visible
            dialog.getWindow().setDimAmount(0.1f); 
        }

        TextView tv = dialog.findViewById(R.id.tvPopupMessage);
        if (tv != null) tv.setText(message);

        dialog.setCancelable(true);
        dialog.show();

        // Auto-dismiss after 1.5 seconds for a snappy feel
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) dialog.dismiss();
        }, 1500);
    }
}
