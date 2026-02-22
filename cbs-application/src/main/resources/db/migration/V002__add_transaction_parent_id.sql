-- Add parent_transaction_id to transactions table to link fee transactions
ALTER TABLE transactions ADD COLUMN parent_transaction_id BIGINT;

-- Add index for better performance when searching for linked fees
CREATE INDEX idx_transactions_parent_id ON transactions(parent_transaction_id);

-- Add foreign key constraint (self-referencing)
ALTER TABLE transactions 
ADD CONSTRAINT fk_transactions_parent 
FOREIGN KEY (parent_transaction_id) 
REFERENCES transactions(id);
