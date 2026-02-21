#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

CONTAINER_NAME="${IT_DB_CONTAINER_NAME:-cbs-it-postgres}"
IT_DB_PORT="${IT_DB_PORT:-55432}"
IT_DB_USER="${IT_DB_USER:-test}"
IT_DB_PASSWORD="${IT_DB_PASSWORD:-test}"
KEEP_CONTAINER="${KEEP_IT_DB_CONTAINER:-false}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker is required but not found in PATH." >&2
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "mvn is required but not found in PATH." >&2
  exit 1
fi

cleanup() {
  if [[ "$KEEP_CONTAINER" != "true" ]]; then
    docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
  fi
}

trap cleanup EXIT

docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
docker run -d \
  --name "$CONTAINER_NAME" \
  -e POSTGRES_DB=postgres \
  -e POSTGRES_USER="$IT_DB_USER" \
  -e POSTGRES_PASSWORD="$IT_DB_PASSWORD" \
  -p "$IT_DB_PORT":5432 \
  postgres:16-alpine >/dev/null

echo "Waiting for PostgreSQL container to become ready..."
for _ in {1..60}; do
  if docker exec "$CONTAINER_NAME" pg_isready -U "$IT_DB_USER" -d postgres >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! docker exec "$CONTAINER_NAME" pg_isready -U "$IT_DB_USER" -d postgres >/dev/null 2>&1; then
  echo "PostgreSQL container did not become ready in time." >&2
  exit 1
fi

DATABASES=(
  "cbs_account_it"
  "cbs_ledger_it"
  "cbs_transaction_it"
  "cbs_payment_it"
  "cbs_loan_it"
  "cbs_deposit_it"
  "cbs_interest_it"
  "cbs_fee_it"
  "cbs_card_it"
  "cbs_fx_it"
  "cbs_notification_it"
  "cbs_reporting_it"
  "cbs_auth_it"
  "cbs_customer_it"
)

for db in "${DATABASES[@]}"; do
  docker exec "$CONTAINER_NAME" psql -U "$IT_DB_USER" -d postgres -v ON_ERROR_STOP=1 \
    -c "SELECT 1 FROM pg_database WHERE datname='${db}'" \
    | grep -q 1 || docker exec "$CONTAINER_NAME" psql -U "$IT_DB_USER" -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE \"${db}\""
done

TEST_MATRIX=(
  "com.cbs.account.service.AccountServicePostgresIntegrationTest:cbs_account_it"
  "com.cbs.ledger.service.LedgerServicePostgresIntegrationTest:cbs_ledger_it"
  "com.cbs.transaction.service.TransactionServicePostgresIntegrationTest:cbs_transaction_it"
  "com.cbs.payment.service.PaymentServicePostgresIntegrationTest:cbs_payment_it"
  "com.cbs.loan.service.LoanServicePostgresIntegrationTest:cbs_loan_it"
  "com.cbs.deposit.service.DepositServicePostgresIntegrationTest:cbs_deposit_it"
  "com.cbs.interest.service.InterestServicePostgresIntegrationTest:cbs_interest_it"
  "com.cbs.fee.service.FeeServicePostgresIntegrationTest:cbs_fee_it"
  "com.cbs.card.service.CardServicePostgresIntegrationTest:cbs_card_it"
  "com.cbs.fx.service.FxServicePostgresIntegrationTest:cbs_fx_it"
  "com.cbs.notification.service.NotificationServicePostgresIntegrationTest:cbs_notification_it"
  "com.cbs.reporting.service.ReportingServicePostgresIntegrationTest:cbs_reporting_it"
  "com.cbs.auth.service.AuthServicePostgresIntegrationTest:cbs_auth_it"
  "com.cbs.customer.service.CustomerServicePostgresIntegrationTest:cbs_customer_it"
)

for entry in "${TEST_MATRIX[@]}"; do
  IFS=':' read -r test_class db_name <<< "$entry"
  echo
  echo "=== Running ${test_class} ==="
  mvn -pl cbs-application test \
    -Dtest="$test_class" \
    -Dsurefire.failIfNoSpecifiedTests=false \
    -Dit.db.url="jdbc:postgresql://localhost:${IT_DB_PORT}/${db_name}" \
    -Dit.db.username="$IT_DB_USER" \
    -Dit.db.password="$IT_DB_PASSWORD"
done

echo
echo "All real PostgreSQL integration tests passed."
