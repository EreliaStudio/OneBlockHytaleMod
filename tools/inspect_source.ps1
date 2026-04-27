param(
    [Parameter(Mandatory = $true)]
    [string]$JarPath,

    [string]$JavaBin = "C:\Program Files\Java\jdk-25.0.2\bin",

    [string]$Patterns = "Knowledge|Recipe|Unlock|Progression|Craft|PlayerRef",

    [string]$ClassName,

    [string]$MethodPattern = "knowledge|unlock|grant|recipe|progress|learn|forget|known",

    [switch]$DeepScan,

    [switch]$ListOnly,

    [switch]$Private,

    [switch]$Bytecode,

    [switch]$VerboseJavap
)

$jarExe   = Join-Path $JavaBin "jar.exe"
$javapExe = Join-Path $JavaBin "javap.exe"

if (!(Test-Path $JarPath)) {
    Write-Error "Jar not found: $JarPath"
    exit 1
}

if (!(Test-Path $jarExe)) {
    Write-Error "jar.exe not found at $jarExe"
    exit 1
}

if (!(Test-Path $javapExe)) {
    Write-Error "javap.exe not found at $javapExe"
    exit 1
}

function Convert-JarEntryToClassName {
    param([string]$Entry)

    return ($Entry -replace "/", "." -replace "\.class$", "")
}

function Invoke-Javap {
    param(
        [string]$TargetClassName,
        [string]$FilterPattern
    )

    $args = @()

    if ($Private) {
        $args += "-private"
    }

    if ($Bytecode) {
        $args += "-c"
    }

    if ($VerboseJavap) {
        $args += "-v"
    }

    $args += "-classpath"
    $args += $JarPath
    $args += $TargetClassName

    Write-Host "`n=== Inspecting class ==="
    Write-Host $TargetClassName
    Write-Host ""

    $output = & $javapExe @args 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Warning "javap failed for: $TargetClassName"
        $output | ForEach-Object { Write-Host $_ }
        return
    }

    if ([string]::IsNullOrWhiteSpace($FilterPattern)) {
        $output | ForEach-Object { Write-Host $_ }
        return
    }

    $matches = $output | Select-String -Pattern $FilterPattern

    if (!$matches) {
        Write-Host "No matching members found for pattern: $FilterPattern"
        Write-Host ""
        Write-Host "Tip: rerun with -MethodPattern `"`" to display the full class."
        return
    }

    Write-Host "=== Matching members / lines ==="
    $matches | ForEach-Object {
        Write-Host $_.Line
    }
}

if ($ClassName) {
    Invoke-Javap -TargetClassName $ClassName -FilterPattern $MethodPattern
    exit 0
}

Write-Host "=== Scanning JAR: $JarPath ===`n"

$entries = & $jarExe tf $JarPath

$matches = $entries | Where-Object {
    $_ -like "*.class" -and $_ -match $Patterns
}

if (!$matches) {
    Write-Host "No matching classes found."
    exit 0
}

Write-Host "=== Matching classes ==="
$matches | ForEach-Object {
    Write-Host $_
}

if ($ListOnly) {
    exit 0
}

if ($DeepScan) {
    Write-Host "`n=== Deep scan ==="

    foreach ($entry in $matches) {
        $targetClassName = Convert-JarEntryToClassName -Entry $entry
        Invoke-Javap -TargetClassName $targetClassName -FilterPattern $MethodPattern
    }
}