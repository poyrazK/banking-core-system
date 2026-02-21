-- Migration to add amortization_type to loans and index on loan_id for loan_schedule_entries

-- Add amortization_type to the loans table
ALTER TABLE loans ADD COLUMN amortization_type VARCHAR(16);

-- Set a default value for existing loan records (assuming ANNUITY for existing loans without type)
UPDATE loans SET amortization_type = 'ANNUITY' WHERE amortization_type IS NULL;

-- Make the column NOT NULL
ALTER TABLE loans ALTER COLUMN amortization_type SET NOT NULL;

-- Create an index to quickly filter schedule entries by loan
CREATE INDEX idx_loan_schedule_loan_id ON loan_schedule_entries (loan_id);
