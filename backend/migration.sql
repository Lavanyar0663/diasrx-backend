-- Migration: Add missing columns for Step 3 and create dispense_logs table

-- 1. Add 'remarks' to 'prescription' table (if it doesn't already exist)
ALTER TABLE `prescription`
ADD COLUMN `remarks` TEXT DEFAULT NULL;

-- 2. Add 'quantity' to 'prescription_drugs' table
ALTER TABLE `prescription_drugs`
ADD COLUMN `quantity` INT DEFAULT NULL;

-- 3. Create 'dispense_logs' table for idempotency
CREATE TABLE `dispense_logs` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `prescription_id` INT NOT NULL,
  `idempotency_key` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_idempotency_prescription` (`prescription_id`, `idempotency_key`),
  CONSTRAINT `dispense_logs_ibfk_1` FOREIGN KEY (`prescription_id`) REFERENCES `prescription` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
