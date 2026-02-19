SELECT 'CREATE DATABASE cbs'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'cbs')\gexec
