#!/usr/bin/env bash
# ============================================================
# healthcheck.sh  — verify the backend /api/health endpoint
# Usage: ./scripts/healthcheck.sh [URL] [MAX_RETRIES] [INTERVAL]
# ============================================================
set -euo pipefail

URL="${1:-http://localhost:8080/api/health}"
MAX_RETRIES="${2:-20}"
INTERVAL="${3:-5}"

echo "==> Healthcheck: $URL (max ${MAX_RETRIES} attempts, ${INTERVAL}s interval)"

for i in $(seq 1 "$MAX_RETRIES"); do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$URL" 2>/dev/null || true)
  if [ "$STATUS" = "200" ]; then
    echo "==> HEALTHY (attempt $i) — HTTP $STATUS"
    exit 0
  fi
  echo "    Attempt $i/$MAX_RETRIES — HTTP ${STATUS:-unreachable}. Retrying in ${INTERVAL}s…"
  sleep "$INTERVAL"
done

echo "==> FAILED: service did not become healthy after $MAX_RETRIES attempts"
exit 1
