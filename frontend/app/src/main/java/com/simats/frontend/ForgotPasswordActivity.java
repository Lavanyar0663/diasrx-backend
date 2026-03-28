package com.simats.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.simats.frontend.databinding.ActivityForgotPasswordBinding;
import com.simats.frontend.network.ApiClient;
import com.simats.frontend.network.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvBackToLogin.setOnClickListener(v -> finish());

        binding.btnSendReset.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            } else {
                performForgotPassword(email);
            }
        });
    }

    private void performForgotPassword(String email) {
        binding.btnSendReset.setEnabled(false);
        binding.btnSendReset.setText("Sending...");

        JsonObject emailData = new JsonObject();
        emailData.addProperty("email", email);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        Call<JsonObject> call = apiInterface.forgotPassword(emailData);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                binding.btnSendReset.setEnabled(true);
                binding.btnSendReset.setText("Send OTP");

                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message").getAsString();
                    String otp = "";
                    if (response.body().has("otp")) {
                        otp = response.body().get("otp").getAsString();
                    }
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                    android.util.Log.d("ForgotPassword", "Success: " + message);
                    Intent intent = new Intent(ForgotPasswordActivity.this, OtpActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("otp", otp);
                    startActivity(intent);
                } else {
                    String errorMsg = "System Error";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            JsonObject errorObj = new com.google.gson.Gson().fromJson(errorJson, JsonObject.class);
                            if (errorObj.has("message")) {
                                errorMsg = errorObj.get("message").getAsString();
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ForgotPassword", "Error parsing error body", e);
                    }
                    
                    android.util.Log.e("ForgotPassword", "Error Code: " + response.code() + " Message: " + errorMsg);
                    Toast.makeText(ForgotPasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                binding.btnSendReset.setEnabled(true);
                binding.btnSendReset.setText("Send OTP");
                android.util.Log.e("ForgotPassword", "Network Error: " + t.getMessage());
                Toast.makeText(ForgotPasswordActivity.this, "Network Error: Please check server", Toast.LENGTH_LONG).show();
            }
        });
    }
}
