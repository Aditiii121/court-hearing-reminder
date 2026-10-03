#!/usr/bin/env bash
# ============================================================
# rollback.sh — roll back to the previous image tag
# Usage: ./scripts/rollback.sh [previous_tag]
# If tag is omitted, reads from docker/.previous_tag
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/../docker"
PREV_TAG_FILE="$DOCKER_DIR/.previous_tag"
HEALTH_URL="${HEALTH_URL:-http://localhost:8080/api/health}"

if [ -n "${1:-}" ]; then
  ROLLBACK_TAG="$1"
else
  if [ ! -f "$PREV_TAG_FILE" ]; then
    echo "==> ERROR: No previous tag file found at $PREV_TAG_FILE and no tag provided."
    exit 1
  fi
  ROLLBACK_TAG=$(cat "$PREV_TAG_FILE")
fi

echo "==> Rolling back to image tag: $ROLLBACK_TAG"

# Stop current containers
echo "==> Stopping current services..."
docker compose -f "$DOCKER_DIR/docker-compose.yml" stop backend frontend

# Start with previous tag
echo "==> Starting services with rollback tag $ROLLBACK_TAG..."
IMAGE_TAG="$ROLLBACK_TAG" docker compose -f "$DOCKER_DIR/docker-compose.yml" up -d backend frontend

# Healthcheck
echo "==> Waiting for rollback services to become healthy..."
"$SCRIPT_DIR/healthcheck.sh" "$HEALTH_URL" 24 5

echo "==> Rollback to $ROLLBACK_TAG complete!"
