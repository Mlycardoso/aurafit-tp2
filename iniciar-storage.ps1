$ErrorActionPreference = 'Stop'
$configPath = Join-Path $PSScriptRoot 'back/.env'
if (!(Test-Path -LiteralPath $configPath)) {
    New-Item -ItemType File -Path $configPath | Out-Null
}
$existing = Get-Content -LiteralPath $configPath -ErrorAction SilentlyContinue
if (!($existing -match '^S3_ACCESS_KEY=')) { Add-Content -LiteralPath $configPath -Encoding ascii -Value "S3_ACCESS_KEY=aurafit-$([Guid]::NewGuid().ToString('N'))" }
if (!($existing -match '^S3_SECRET_KEY=')) { Add-Content -LiteralPath $configPath -Encoding ascii -Value "S3_SECRET_KEY=$([Guid]::NewGuid().ToString('N'))$([Guid]::NewGuid().ToString('N'))" }
if (!($existing -match '^S3_ENDPOINT=')) { Add-Content -LiteralPath $configPath -Encoding ascii -Value 'S3_ENDPOINT=http://localhost:9000' }
if (!($existing -match '^S3_REGION=')) { Add-Content -LiteralPath $configPath -Encoding ascii -Value 'S3_REGION=us-east-1' }
if (!($existing -match '^S3_BUCKET=')) { Add-Content -LiteralPath $configPath -Encoding ascii -Value 'S3_BUCKET=aurafit-produtos' }
docker compose --env-file $configPath -f (Join-Path $PSScriptRoot 'compose.storage.yaml') up -d
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível iniciar o RustFS. Verifique se o Docker Desktop está em execução.' }
Write-Host 'RustFS iniciado. Console: http://localhost:9001. Credenciais locais em back/.env (não versionar).'
