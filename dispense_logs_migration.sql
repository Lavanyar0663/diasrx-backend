CREATE TABLE IF NOT EXISTS dispense_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prescription_id INT NOT LUUL,
    idempotency_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_id) REFERENCES prescription(id) ON DELETE CASCADE
);

CREATE INDEX idx_idempotency ON dispense_logs (prescription_id, idempotency_key);
