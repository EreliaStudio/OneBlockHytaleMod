param(
    [string]$ServerRoot = "$env:APPDATA\Hytale\install\release\package\game\latest\Server",
    [string]$Java = "$env:APPDATA\Hytale\install\release\package\jre\latest\bin\java.exe",
    [string]$Jar = "HytaleServer.jar",
    [string[]]$Args = @()
)

$serverJar = Join-Path $ServerRoot $Jar

if (!(Test-Path $Java)) {
    Write-Error "Java not found: $Java"
    exit 1
}
if (!(Test-Path $serverJar)) {
    Write-Error "Server jar not found: $serverJar"
    exit 1
}

# Stop running server (java -jar HytaleServer.jar)
$javaProcs = Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -like "*$serverJar*" -or $_.CommandLine -like "*HytaleServer.jar*" }

foreach ($p in $javaProcs) {
    Stop-Process -Id $p.ProcessId -Force
}

Start-Sleep -Seconds 1

# Start server in the current terminal (foreground) so output is visible
Set-Location $ServerRoot
$argList = @("-jar", $serverJar) + $Args
& $Java @argList
