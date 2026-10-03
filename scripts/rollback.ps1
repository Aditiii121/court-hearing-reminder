param(
    [string]$RollbackTag = ""
)
# ============================================================
# rollback.ps1 — roll back to previous image tag (Windows)
# Usage: .\scripts\rollback.ps1 [-RollbackTag <tag>]
# ============================================================
$ErrorActionPreference = "Stop"
$ScriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$DockerDir   = Join-Path $ScriptDir "..\docker"
$PrevTagFile = Join-Path $DockerDir ".previous_tag"
$HealthUrl   = if ($env:HEALTH_URL) { $env:HEALTH_URL } else { "http://localhost:8080/api/health" }

if (-not $RollbackTag) {
    if (-not (Test-Path $PrevTagFile)) {
        Write-Error "No previous tag file found and no -RollbackTag provided."
        exit 1
    }
    $RollbackTag = (Get-Content $PrevTagFile).Trim()
}

Write-Host "==> Rolling back to image tag: $RollbackTag"

Write-Host "==> Stopping current backend and frontend..."
docker compose -f "$DockerDir\docker-compose.yml" stop backend frontend

Write-Host "==> Starting services with rollback tag $RollbackTag..."
$env:IMAGE_TAG = $RollbackTag
docker compose -f "$DockerDir\docker-compose.yml" up -d backend frontend

Write-Host "==> Running healthcheck..."
& "$ScriptDir\healthcheck.ps1" -Url $HealthUrl -MaxRetries 24 -Interval 5

Write-Host "==> Rollback to $RollbackTag complete!"
