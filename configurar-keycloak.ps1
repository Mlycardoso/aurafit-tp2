$ErrorActionPreference = 'Stop'

$configPath = Join-Path $PSScriptRoot 'back/.env'
if (!(Test-Path -LiteralPath $configPath)) {
    throw 'back/.env nao existe. Execute iniciar-keycloak.ps1 primeiro.'
}

function Get-EnvValue([string[]]$Lines, [string]$Name) {
    $line = $Lines | Where-Object { $_ -match ( '^' + [regex]::Escape($Name) + '=' ) } | Select-Object -First 1
    if ($null -eq $line) { return $null }
    return $line.Substring($Name.Length + 1)
}

function Set-EnvValue([string]$Name, [string]$Value) {
    $script:lines = @($script:lines | Where-Object { $_ -notmatch ( '^' + [regex]::Escape($Name) + '=' ) })
    $script:lines += "$Name=$Value"
}

$lines = @(Get-Content -LiteralPath $configPath)
$bootstrapUser = Get-EnvValue $lines 'KEYCLOAK_BOOTSTRAP_USER'
$bootstrapPassword = Get-EnvValue $lines 'KEYCLOAK_BOOTSTRAP_PASSWORD'
if ([string]::IsNullOrWhiteSpace($bootstrapUser) -or [string]::IsNullOrWhiteSpace($bootstrapPassword)) {
    throw 'KEYCLOAK_BOOTSTRAP_USER ou KEYCLOAK_BOOTSTRAP_PASSWORD ausente em back/.env. Execute iniciar-keycloak.ps1.'
}

$baseUrl = Get-EnvValue $lines 'KEYCLOAK_SERVER_URL'
if ([string]::IsNullOrWhiteSpace($baseUrl)) { $baseUrl = 'http://127.0.0.1:8081' }
$baseUrl = $baseUrl.TrimEnd('/')
$tokenUrl = "$baseUrl/realms/master/protocol/openid-connect/token"
$adminRoot = "$baseUrl/admin/realms/aurafit"

try {
    $tokenResponse = Invoke-RestMethod -Method Post -Uri $tokenUrl -ContentType 'application/x-www-form-urlencoded' -Body @{
        grant_type = 'password'; client_id = 'admin-cli'; username = $bootstrapUser; password = $bootstrapPassword
    }
} catch {
    throw 'Nao foi possivel acessar o Keycloak ou obter token de administrador. Confirme que ele esta no ar em http://127.0.0.1:8081 e que as credenciais master de back/.env estao corretas.'
}
$headers = @{ Authorization = "Bearer $($tokenResponse.access_token)" }

try {
    $apiClient = Invoke-RestMethod -Method Get -Uri "$adminRoot/clients?clientId=aurafit-api" -Headers $headers
    $adminClient = Invoke-RestMethod -Method Get -Uri "$adminRoot/clients?clientId=aurafit-admin" -Headers $headers
    if (!$apiClient -or !$adminClient) { throw 'clients ausentes' }
    $apiSecret = (Invoke-RestMethod -Method Get -Uri "$adminRoot/clients/$($apiClient[0].id)/client-secret" -Headers $headers).value
    $adminSecret = (Invoke-RestMethod -Method Get -Uri "$adminRoot/clients/$($adminClient[0].id)/client-secret" -Headers $headers).value
    $users = Invoke-RestMethod -Method Get -Uri "$adminRoot/users?username=admin%40aurafit.local&exact=true" -Headers $headers
    if (!$users) { throw 'usuário admin ausente' }

    $bytes = New-Object byte[] 36
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $adminPassword = [Convert]::ToBase64String($bytes)
    $credential = @{ type = 'password'; value = $adminPassword; temporary = $false } | ConvertTo-Json
    Invoke-RestMethod -Method Put -Uri "$adminRoot/users/$($users[0].id)/reset-password" -Headers $headers -ContentType 'application/json' -Body $credential | Out-Null

    Set-EnvValue 'KEYCLOAK_API_CLIENT_SECRET' $apiSecret
    Set-EnvValue 'KEYCLOAK_ADMIN_CLIENT_SECRET' $adminSecret
    Set-EnvValue 'AURAFIT_ADMIN_PASSWORD' $adminPassword
    Set-Content -LiteralPath $configPath -Value $lines -Encoding ascii
} catch {
    throw 'Configuracao incompleta: confirme que o realm aurafit foi importado e contem os clients e o usuario admin@aurafit.local. Nenhum secret foi exibido.'
}

Write-Host "Configuracao concluida. Senha de desenvolvimento de admin@aurafit.local (exibida uma vez): $adminPassword"
