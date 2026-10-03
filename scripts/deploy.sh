#!/usr/bin/env bash
# ============================================================
# deploy.sh — deploy a new image version
# Usage: ./scripts/deploy.sh <image_tag>
# Saves previous tag for rollback.
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/../docker"
NEW_TAG="${1:?Usage: $0 <image_tag>}"
PREV_TAG_FILE="$DOCKER_DIR/.previous_tag"
HEALTH_URL="${HEALTH_URL:-http://localhost:8080/api/health}"

echo "==> Deploying image tag: $NEW_TAG"

# Save current tag for rollback
if [ -f "$PREV_TAG_FILE" ]; then
  echo "==> Previous tag: $(cat "$PREV_TAG_FILE")"
fi
echo "$NEW_TAG" > "$PREV_TAG_FILE"

# Pull new images
echo "==> Pulling images..."
IMAGE_TAG="$NEW_TAG" docker compose -f "$DOCKER_DIR/docker-compose.yml" pull backend frontend || true

# Update running containers
echo "==> Starting services with tag $NEW_TAG..."
IMAGE_TAG="$NEW_TAG" docker compose -f "$DOCKER_DIR/docker-compose.yml" up -d --no-build

# Healthcheck
echo "==> Waiting for services to become healthy..."
"$SCRIPT_DIR/healthcheck.sh" "$HEALTH_URL" 24 5

echo "==> Deploy of $NEW_TAG complete!"
