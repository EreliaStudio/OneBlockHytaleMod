param(
    [string]$RepoRoot = $PSScriptRoot,
    [string]$Java = "$env:APPDATA\Hytale\install\release\package\jre\latest\bin\java.exe",
    [string[]]$Args = @()
)

# Use the installed server JAR (matches the downloaded Assets.zip version)
$serverJar  = "$env:APPDATA\Hytale\install\release\package\game\latest\Server\HytaleServer.jar"
$workdir    = Join-Path $RepoRoot "hytale-server"

if (!(Test-Path $Java)) {
    Write-Error "Java not found: $Java"
    exit 1
}
if (!(Test-Path $serverJar)) {
    Write-Error "Server jar not found: $serverJar"
    exit 1
}

# Stop any running server instance
$javaProcs = Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -like "*HytaleServer.jar*" }

foreach ($p in $javaProcs) {
    Stop-Process -Id $p.ProcessId -Force
    Write-Host "Stopped server process $($p.ProcessId)"
}

if ($javaProcs) { Start-Sleep -Seconds 1 }

# Start server from hytale-server/ so config.json and mods/ are picked up correctly
Set-Location $workdir
$argList = @("-jar", $serverJar) + $Args
Write-Host "Starting: $Java $argList  (workdir: $workdir)"
& $Java @argList
