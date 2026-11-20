$ErrorActionPreference = 'Stop'
$configPath = Join-Path $PSScriptRoot 'back/.env'
if (!(Test-Path -LiteralPath $configPath)) {
    $access = 'aurafit-' + [Guid]::NewGuid().ToString('N')
    $secret = [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
    @("S3_ACCESS_KEY=$access", "S3_SECRET_KEY=$secret", 'S3_ENDPOINT=http://localhost:9000', 'S3_REGION=us-east-1', 'S3_BUCKET=aurafit-produtos') |
        Set-Content -LiteralPath $configPath -Encoding ascii
}
docker compose --env-file $configPath -f (Join-Path $PSScriptRoot 'compose.storage.yaml') up -d
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível iniciar o RustFS. Verifique se o Docker Desktop está em execução.' }
Write-Host 'RustFS iniciado. Console: http://localhost:9001. Credenciais locais em back/.env (não versionar).'
