$ErrorActionPreference = 'Stop'
$configPath = Join-Path $PSScriptRoot 'back/.env'
if (!(Test-Path -LiteralPath $configPath)) {
    New-Item -ItemType File -Path $configPath | Out-Null
}
$existing = Get-Content -LiteralPath $configPath -ErrorAction SilentlyContinue
if (!($existing -match '^KEYCLOAK_BOOTSTRAP_USER=')) {
    Add-Content -LiteralPath $configPath -Encoding ascii -Value "KEYCLOAK_BOOTSTRAP_USER=aurafit-admin"
}
if (!($existing -match '^KEYCLOAK_BOOTSTRAP_PASSWORD=')) {
    $password = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    Add-Content -LiteralPath $configPath -Encoding ascii -Value "KEYCLOAK_BOOTSTRAP_PASSWORD=$password"
}
$realmPath = Join-Path $PSScriptRoot 'keycloak'
New-Item -ItemType Directory -Force -Path $realmPath | Out-Null
docker compose --env-file $configPath -f (Join-Path $PSScriptRoot 'compose.keycloak.yaml') up -d
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível iniciar o Keycloak. Verifique se o Docker Desktop está em execução.' }
Write-Host 'Keycloak iniciado em http://127.0.0.1:8081. Credenciais locais em back/.env (não versionar).'
