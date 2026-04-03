-- Migration: Add OTP and OTP Expiry columns to users table
ALTER TABLE `users`
ADD COLUMN `otp` VARCHAR(6) DEFAULT NULL,
ADD COLUMN `otp_expiry` TIMESTAMP NULL DEFAULT NULL;
