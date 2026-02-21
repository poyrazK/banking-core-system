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

TEST_CLASSES=(
  "com.cbs.account.service.AccountServicePostgresIntegrationTest"
  "com.cbs.ledger.service.LedgerServicePostgresIntegrationTest"
  "com.cbs.transaction.service.TransactionServicePostgresIntegrationTest"
  "com.cbs.payment.service.PaymentServicePostgresIntegrationTest"
  "com.cbs.loan.service.LoanServicePostgresIntegrationTest"
  "com.cbs.deposit.service.DepositServicePostgresIntegrationTest"
  "com.cbs.interest.service.InterestServicePostgresIntegrationTest"
  "com.cbs.fee.service.FeeServicePostgresIntegrationTest"
  "com.cbs.card.service.CardServicePostgresIntegrationTest"
  "com.cbs.fx.service.FxServicePostgresIntegrationTest"
  "com.cbs.notification.service.NotificationServicePostgresIntegrationTest"
  "com.cbs.reporting.service.ReportingServicePostgresIntegrationTest"
  "com.cbs.auth.service.AuthServicePostgresIntegrationTest"
  "com.cbs.customer.service.CustomerServicePostgresIntegrationTest"
)

for test_class in "${TEST_CLASSES[@]}"; do
  domain=$(echo "$test_class" | cut -d. -f3)
  db_name="cbs_${domain}_it"
  echo
  echo "=== Running ${domain} / ${test_class} ==="
  mvn -pl :cbs-application test \
    -Dtest="$test_class" \
    -Dsurefire.failIfNoSpecifiedTests=false \
    -Dit.db.url="jdbc:postgresql://localhost:${IT_DB_PORT}/${db_name}" \
    -Dit.db.username="$IT_DB_USER" \
    -Dit.db.password="$IT_DB_PASSWORD"
done

echo
echo "All real PostgreSQL integration tests passed."
