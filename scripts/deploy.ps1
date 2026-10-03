param(
    [Parameter(Mandatory)][string]$ImageTag
)
# ============================================================
# deploy.ps1 — deploy a new image version (Windows)
# Usage: .\scripts\deploy.ps1 -ImageTag <tag>
# ============================================================
$ErrorActionPreference = "Stop"
$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$DockerDir  = Join-Path $ScriptDir "..\docker"
$PrevTagFile = Join-Path $DockerDir ".previous_tag"
$HealthUrl  = if ($env:HEALTH_URL) { $env:HEALTH_URL } else { "http://localhost:8080/api/health" }

Write-Host "==> Deploying image tag: $ImageTag"

if (Test-Path $PrevTagFile) {
    Write-Host "==> Previous tag: $(Get-Content $PrevTagFile)"
}
Set-Content -Path $PrevTagFile -Value $ImageTag

Write-Host "==> Pulling images..."
$env:IMAGE_TAG = $ImageTag
docker compose -f "$DockerDir\docker-compose.yml" pull backend frontend

Write-Host "==> Starting services..."
docker compose -f "$DockerDir\docker-compose.yml" up -d --no-build

Write-Host "==> Running healthcheck..."
& "$ScriptDir\healthcheck.ps1" -Url $HealthUrl -MaxRetries 24 -Interval 5

Write-Host "==> Deploy of $ImageTag complete!"
