-- Migration Step 1: Database Architecture & Unified Schema Upgrade

-- 1. Create unified users table
CREATE TABLE IF NOT EXISTS `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` ENUM('admin', 'doctor', 'pharmacist', 'patient') NOT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Modify existing admin, doctors, pharmacists tables to add user_id
ALTER TABLE `admin` 
  ADD COLUMN `user_id` int DEFAULT NULL,
  ADD CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `doctors` 
  ADD COLUMN `user_id` int DEFAULT NULL,
  ADD CONSTRAINT `fk_doctors_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `pharmacists` 
  ADD COLUMN `user_id` int DEFAULT NULL,
  ADD CONSTRAINT `fk_pharmacists_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

-- 3. Modify prescription table to enforce ENUM
-- First, normalize existing records containing 'pending' status
UPDATE `prescription` SET `status` = 'CREATED' WHERE `status` = 'pending';

-- Alter the status column to strict ENUM
ALTER TABLE `prescription` 
  MODIFY COLUMN `status` ENUM('CREATED', 'DISPENSED', 'CANCELLED') DEFAULT 'CREATED';

-- 4. Add Performance Indexes
-- patients(doctor_id)
ALTER TABLE `patients` 
  ADD INDEX `idx_patients_doctor` (`doctor_id`);

-- prescription(patient_id)
ALTER TABLE `prescription` 
  ADD INDEX `idx_prescription_patient` (`patient_id`);

-- prescription(doctor_id)
ALTER TABLE `prescription` 
  ADD INDEX `idx_prescription_doctor` (`doctor_id`);

-- prescription(patient_id, doctor_id)
ALTER TABLE `prescription` 
  ADD INDEX `idx_prescription_patient_doctor_combo` (`patient_id`, `doctor_id`);

-- 5. Add Soft Delete (is_active) Flags
ALTER TABLE `patients` 
  ADD COLUMN `is_active` BOOLEAN DEFAULT TRUE;

ALTER TABLE `drug_master` 
  ADD COLUMN `is_active` BOOLEAN DEFAULT TRUE;
