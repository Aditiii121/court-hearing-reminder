# ============================================================
# healthcheck.ps1 — verify the backend /api/health endpoint
# Usage: .\scripts\healthcheck.ps1 [-Url <url>] [-MaxRetries <n>] [-Interval <sec>]
# ============================================================
param(
    [string]$Url = "http://localhost:8080/api/health",
    [int]$MaxRetries = 20,
    [int]$Interval = 5
)

Write-Host "==> Healthcheck: $Url (max $MaxRetries attempts, ${Interval}s interval)"

for ($i = 1; $i -le $MaxRetries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "==> HEALTHY (attempt $i) — HTTP $($response.StatusCode)"
            exit 0
        }
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "unreachable" }
        Write-Host "    Attempt $i/$MaxRetries — HTTP $status. Retrying in ${Interval}s..."
    }
    Start-Sleep -Seconds $Interval
}

Write-Error "==> FAILED: service did not become healthy after $MaxRetries attempts"
exit 1
