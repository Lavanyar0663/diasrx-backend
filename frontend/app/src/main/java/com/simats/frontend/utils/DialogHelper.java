package com.simats.frontend.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.simats.frontend.R;

public class DialogHelper {

    private DialogHelper() {
        // Private constructor to prevent instantiation
    }

    public interface DialogCallback {
        void onConfirm();
    }

    public interface InputCallback {
        void onInput(String text);
    }

    public static void showLogoutDialog(Context context, DialogCallback callback) {
        showCustomDialog(context, 
                R.drawable.ic_logout, 
                "Confirm Logout", 
                "Are you sure you want to log out of DIAS Rx? Your session will be ended.", 
                "Logout", 
                "Cancel", 
                callback);
    }

    public static void showConfirmationDialog(Context context, int iconResId, String title, String message, String posText, String negText, DialogCallback callback) {
        showCustomDialog(context, iconResId, title, message, posText, negText, callback);
    }

    private static void showCustomDialog(Context context, int iconResId, String title, String message, String posText, String negText, DialogCallback callback) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dias_rx_dialog, null);
        dialog.setContentView(view);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivIcon = view.findViewById(R.id.ivDialogIcon);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        ivIcon.setImageResource(iconResId);
        tvTitle.setText(title);
        tvMessage.setText(message);
        btnConfirm.setText(posText);
        btnCancel.setText(negText);

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (callback != null) callback.onConfirm();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static void showInputDialog(Context context, String title, String hint, String posText, String negText, InputCallback callback) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_dias_rx_input_dialog, null);
        dialog.setContentView(view);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        android.widget.EditText etInput = view.findViewById(R.id.etDialogInput);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        tvTitle.setText(title);
        etInput.setHint(hint);
        btnConfirm.setText(posText);
        btnCancel.setText(negText);

        btnConfirm.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!text.isEmpty()) {
                dialog.dismiss();
                if (callback != null) callback.onInput(text);
            } else {
                etInput.setError("Field cannot be empty");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
