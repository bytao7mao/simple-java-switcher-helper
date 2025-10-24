# =========================================================
# Java Switcher Script for Windows (PowerShell 5.1+ / 7)
# Allows temporary or permanent JAVA_HOME switching
# =========================================================

# Function: select from list by number
function Select-FromList {
    param (
        [Parameter(Mandatory)][string[]]$Options,
        [string]$Prompt = "Select an option"
    )

    for ($i=0; $i -lt $Options.Count; $i++) {
        Write-Host "[$i] $($Options[$i])"
    }

    do {
        $selection = Read-Host "$Prompt (Enter number, empty = latest installed [0])"
        if ($selection -eq "") { $selection = 0; break }
        if ($selection -match '^\d+$' -and $selection -ge 0 -and $selection -lt $Options.Count) { break }
        Write-Host "Invalid selection. Enter a number between 0 and $($Options.Count - 1)." -ForegroundColor Red
    } while ($true)

    return [int]$selection
}

# Detect installed JDKs
$jdkRoots = @(
    "C:\Program Files\Java",
    "C:\Program Files (x86)\Java"
)

$jdks = @()
foreach ($root in $jdkRoots) {
    if (Test-Path $root) {
        Get-ChildItem -Path $root -Directory | ForEach-Object { 
            $javaExe = Join-Path $_.FullName "bin\java.exe"
            if (Test-Path $javaExe) { $jdks += $_.FullName }
        }
    }
}

# Validate detected JDKs
$validJdks = @()
foreach ($jdk in $jdks) {
    try {
        $ver = & "$jdk\bin\java.exe" -version 2>&1 | Select-String 'version'
        $validJdks += "$jdk => $($ver.Line)"
    } catch {
        $validJdks += "$jdk => Invalid or incompatible Java executable"
    }
}

# Show list
Write-Host "Detected JDKs:"
$selectedIndex = Select-FromList -Options $validJdks

$selectedJdk = $jdks[$selectedIndex]
Write-Host "Selected JDK: $selectedJdk"

# Temporary or permanent?
$mode = Read-Host "Switch temporarily for session or permanently? (temp/permanent)"
if ($mode -eq "") { $mode = "temp" }

# Temporary switch
if ($mode -eq "temp") {
    $env:JAVA_HOME = $selectedJdk
    $env:Path = "$env:JAVA_HOME\bin;" + ($env:Path -split ";") -join ";"
    Write-Host "JAVA_HOME switched temporarily to $selectedJdk" -ForegroundColor Green
    java -version
    return
}

# Permanent switch
if ($mode -eq "permanent") {
    try {
        # Backup system PATH
        $desktopBackup = [Environment]::GetFolderPath("Desktop") + "\PathBackup.txt"
        [Environment]::GetEnvironmentVariable("Path","Machine") | Out-File -FilePath $desktopBackup
        Write-Host "Backup of system Path saved to Desktop."

        # Update JAVA_HOME
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $selectedJdk, [System.EnvironmentVariableTarget]::Machine)

        # Remove old JDK paths from Path
        $oldPath = [Environment]::GetEnvironmentVariable("Path","Machine")
        $pathParts = $oldPath -split ";"
        $pathParts = $pathParts | Where-Object {$_ -notmatch "Java\\jdk"}
        $newPath = "$selectedJdk\bin;" + ($pathParts -join ";")
        [Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::Machine)

        Write-Host "JAVA_HOME and Path updated permanently." -ForegroundColor Cyan
        Write-Host "Restart PowerShell or your PC for changes to take effect."

        # Temporary update for current session
        $env:JAVA_HOME = $selectedJdk
        $env:Path = "$env:JAVA_HOME\bin;" + ($env:Path -split ";") -join ";"
        java -version

    } catch {
        Write-Host "Failed to update system environment variables. Run PowerShell as Administrator." -ForegroundColor Red
    }
} else {
    Write-Host "Unknown mode. No changes applied." -ForegroundColor Yellow
}
