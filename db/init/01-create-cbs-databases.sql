SELECT 'CREATE DATABASE cbs_auth'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_auth')\gexec

SELECT 'CREATE DATABASE cbs_customer'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_customer')\gexec

SELECT 'CREATE DATABASE cbs_account'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_account')\gexec

SELECT 'CREATE DATABASE cbs_ledger'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_ledger')\gexec

SELECT 'CREATE DATABASE cbs_transaction'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_transaction')\gexec

SELECT 'CREATE DATABASE cbs_payment'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_payment')\gexec

SELECT 'CREATE DATABASE cbs_loan'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_loan')\gexec

SELECT 'CREATE DATABASE cbs_deposit'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_deposit')\gexec

SELECT 'CREATE DATABASE cbs_card'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_card')\gexec

SELECT 'CREATE DATABASE cbs_notification'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_notification')\gexec

SELECT 'CREATE DATABASE cbs_interest'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_interest')\gexec

SELECT 'CREATE DATABASE cbs_fee'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_fee')\gexec

SELECT 'CREATE DATABASE cbs_fx'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_fx')\gexec

SELECT 'CREATE DATABASE cbs_reporting'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs_reporting')\gexec
