package com.simats.frontend.network;

import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiInterface {

    // Auth
    @POST("api/auth/login")
    Call<JsonObject> login(@Body JsonObject credentials);

    @POST("api/auth/register")
    Call<JsonObject> registerUser(@Body JsonObject userData);

    @POST("api/auth/forgot-password")
    Call<JsonObject> forgotPassword(@Body JsonObject emailData);

    @POST("api/auth/verify-otp")
    Call<JsonObject> verifyOtp(@Body JsonObject otpData);

    @POST("api/auth/reset-password")
    Call<JsonObject> resetPassword(@Body JsonObject resetData);

    // Patients
    @GET("api/patients")
    Call<List<JsonObject>> getPatients();

    // Prescriptions
    @POST("api/prescriptions/full")
    Call<JsonObject> createFullPrescription(@Body JsonObject prescriptionData);

    @PATCH("api/prescriptions/{id}/dispense")
    Call<JsonObject> dispensePrescription(
            @Path("id") String prescriptionId,
            @Header("Idempotency-Key") String idempotencyKey);

    @GET("api/prescriptions/patient/{id}")
    Call<List<JsonObject>> getPatientPrescriptions(@Path("id") String patientId);

    @GET("api/prescriptions/doctor/{id}")
    Call<List<JsonObject>> getPrescriptionsByDoctor(@Path("id") String doctorId);

    // New endpoint strictly for Pharmacist dashboard to see all pending
    // prescriptions in system
    @GET("api/prescriptions/pending")
    Call<List<JsonObject>> getPendingPrescriptions();

    @GET("api/prescriptions/history")
    Call<List<JsonObject>> getPrescriptionHistory();

    @GET("api/prescriptions/stats")
    Call<JsonObject> getPharmacistStats();

    @GET("api/prescriptions/{id}")
    Call<JsonObject> getPrescriptionDetails(@Path("id") String prescriptionId);

    // Drugs
    @GET("api/drugs/search")
    Call<List<JsonObject>> searchDrugs(@Query("name") String name);

    // Profile
    @GET("api/doctors/profile")
    Call<JsonObject> getDoctorProfile();

    @PATCH("api/doctors/profile")
    Call<JsonObject> updateDoctorProfile(@Body JsonObject profileData);

    @PATCH("api/auth/change-password")
    Call<JsonObject> changePassword(@Body JsonObject passwordData);

    // Admin
    @GET("api/admin/requests")
    Call<List<JsonObject>> getPendingRequests();

    @GET("api/admin/requests/all")
    Call<List<JsonObject>> getAllRequests();

    @PATCH("api/admin/requests/{id}/approve")
    Call<JsonObject> approveRequest(@Path("id") String id);

    @PATCH("api/admin/requests/{id}/reject")
    Call<JsonObject> rejectRequest(@Path("id") String id);

    @GET("api/admin/stats")
    Call<JsonObject> getAdminStats();

    @GET("api/admin/doctors")
    Call<List<JsonObject>> getDoctors();

    @GET("api/admin/pharmacists")
    Call<List<JsonObject>> getPharmacists();

    @POST("api/auth/request-access")
    Call<JsonObject> requestAccess(@Body JsonObject accessData);
}
