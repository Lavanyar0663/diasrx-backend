package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityOtpBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = getIntent().getStringExtra("email");
        String demoOtp = getIntent().getStringExtra("otp");

        // Back button work properly
        binding.btnBack.setOnClickListener(v -> finish());

        setupOtpInputs();

        // Auto-fill for demo purposes
        if (demoOtp != null && demoOtp.length() == 6) {
            binding.etOtp1.setText(String.valueOf(demoOtp.charAt(0)));
            binding.etOtp2.setText(String.valueOf(demoOtp.charAt(1)));
            binding.etOtp3.setText(String.valueOf(demoOtp.charAt(2)));
            binding.etOtp4.setText(String.valueOf(demoOtp.charAt(3)));
            binding.etOtp5.setText(String.valueOf(demoOtp.charAt(4)));
            binding.etOtp6.setText(String.valueOf(demoOtp.charAt(5)));
            Toast.makeText(this, "Demo Mode: OTP Auto-filled", Toast.LENGTH_LONG).show();
            // Automatically clear focus to hide keyboard
            binding.etOtp6.clearFocus();
        }

        binding.btnVerify.setOnClickListener(v -> {
            String otp = getOtpFromInputs();
            if (otp.length() < 6) {
                Toast.makeText(this, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyOtp(otp);
            }
        });
    }

    private void setupOtpInputs() {
        EditText[] inputs = {binding.etOtp1, binding.etOtp2, binding.etOtp3, binding.etOtp4, binding.etOtp5, binding.etOtp6};
        
        for (int i = 0; i < inputs.length; i++) {
            final int index = i;
            inputs[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1) {
                        if (index < inputs.length - 1) {
                            inputs[index + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                        if (index > 0) {
                            inputs[index - 1].requestFocus();
                        }
                    }
                }
            });
        }
    }

    private String getOtpFromInputs() {
        return binding.etOtp1.getText().toString() +
               binding.etOtp2.getText().toString() +
               binding.etOtp3.getText().toString() +
               binding.etOtp4.getText().toString() +
               binding.etOtp5.getText().toString() +
               binding.etOtp6.getText().toString();
    }

    private void verifyOtp(String otp) {
        binding.btnVerify.setEnabled(false);
        binding.btnVerify.setText("Verifying...");

        JsonObject otpData = new JsonObject();
        otpData.addProperty("email", email);
        otpData.addProperty("otp", otp);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.verifyOtp(otpData);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.btnVerify.setEnabled(true);
                binding.btnVerify.setText("Verify & Proceed");

                if (response.isSuccessful()) {
                    android.util.Log.d("OtpActivity", "OTP Verified Successfully");
                    Intent intent = new Intent(OtpActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("otp", otp);
                    startActivity(intent);
                } else {
                    android.util.Log.e("OtpActivity", "OTP Verification Failed: " + response.code());
                    Toast.makeText(OtpActivity.this, "Invalid or expired OTP", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnVerify.setEnabled(true);
                binding.btnVerify.setText("Verify & Proceed");
                android.util.Log.e("OtpActivity", "Network Error: " + t.getMessage());
                Toast.makeText(OtpActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
