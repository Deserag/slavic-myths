# save as merge-targets.ps1
# run: powershell -ExecutionPolicy Bypass -File .\merge-targets.ps1

$ErrorActionPreference = "Stop"

$Variants = "C:\Users\user\Documents\slavic-myths\.git\worktrees\slavic-myths\recovery-20261102T050215Z-1893\variants"

$Repo = (& git rev-parse --show-toplevel).Trim()
if ($LASTEXITCODE -ne 0) { throw "Git repository not found" }
Set-Location $Repo

$Branch = (& git branch --show-current).Trim()
if ($Branch -ne "hell-2.1") { throw "Wrong branch: $Branch. Expected hell-2.1" }
if (!(Test-Path -LiteralPath $Variants -PathType Container)) { throw "Variants directory not found: $Variants" }

# 1. Find the highest numbered variant
$Latest = Get-ChildItem -LiteralPath $Variants -Directory |
    Where-Object { $_.Name -match '^\d+$' } |
    Sort-Object { [long]$_.Name } -Descending |
    Select-Object -First 1

if (!$Latest) { throw "No numbered variant directories found" }

Write-Host "SOURCE VARIANT: $($Latest.Name)" -ForegroundColor Cyan
Write-Host "TARGET BRANCH: $Branch" -ForegroundColor Cyan

# 2. Search keywords
$Keywords = '(?i)(Nether|Goose|Bauk|Bauq|Villager|Egg|Economy|Food|Recipe|Dish|Item|Entit(?:y|ies))'
$Specific = '(?i)(Nether|Goose|Bauk|Bauq|Villager|Egg|Economy|Food|Dish|Recipe)'
$VisualSpecific = '(?i)(Nether|Goose|Bauk|Bauq|Villager|Egg|Economy|Food|Dish)'

# 3. Prepare backups and logs
$Stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$Report = Join-Path (Split-Path $Variants -Parent) "targeted-merge-$Stamp"
$Backup = Join-Path $Report "backup"
$Log = Join-Path $Report "merged-files.txt"
$BuildLog = Join-Path $Report "compileJava.log"

New-Item -ItemType Directory -Path $Backup -Force | Out-Null

$Copied = 0
$Skipped = 0
$Failed = 0
$Results = [System.Collections.Generic.List[string]]::new()

# 4. Restore only targeted files
$Files = Get-ChildItem -LiteralPath $Latest.FullName -File -Recurse

foreach ($File in $Files) {
    $Relative = $File.FullName.Substring($Latest.FullName.Length).TrimStart('\','/')
    $Relative = $Relative.Replace('\','/')

    if ($Relative -notmatch '^src/main/(java|resources)/') { continue }
    if ($Relative -notmatch $Keywords) { continue }

    $IsJava = $Relative -like 'src/main/java/*'
    $Extension = $File.Extension.ToLowerInvariant()

    # Generic Item/Entity paths alone do not justify importing resources.
    if (!$IsJava -and $Relative -notmatch $Specific) {
        $Skipped++
        continue
    }

    # Exclude unrelated textures and item/block/entity models.
    $IsVisual = $Extension -eq ".png" -or ($Extension -eq ".json" -and $Relative -match '(?i)(^|/)models/')

    if ($IsVisual -and $Relative -notmatch $VisualSpecific) {
        $Skipped++
        continue
    }

    $Target = Join-Path $Repo $Relative
    $BackupFile = Join-Path $Backup $Relative

    try {
        if (Test-Path -LiteralPath $Target -PathType Leaf) {
            $OldHash = (Get-FileHash -LiteralPath $Target -Algorithm SHA256).Hash
            $NewHash = (Get-FileHash -LiteralPath $File.FullName -Algorithm SHA256).Hash

            if ($OldHash -eq $NewHash) {
                $Skipped++
                continue
            }

            New-Item -ItemType Directory -Path (Split-Path $BackupFile -Parent) -Force | Out-Null
            Copy-Item -LiteralPath $Target -Destination $BackupFile -Force
        }

        New-Item -ItemType Directory -Path (Split-Path $Target -Parent) -Force | Out-Null
        Copy-Item -LiteralPath $File.FullName -Destination $Target -Force

        $Copied++
        $Results.Add("[$($Latest.Name)] REPLACED -> $Relative")
        Write-Host "RESTORED: $Relative" -ForegroundColor Green
    }
    catch {
        $Failed++
        $Results.Add("ERROR: $Relative -> $($_.Exception.Message)")
        Write-Host "FAILED: $Relative" -ForegroundColor Red
    }
}

$Results | Set-Content -LiteralPath $Log -Encoding UTF8

Write-Host ""
Write-Host "========== MERGE RESULTS ==========" -ForegroundColor Cyan
Write-Host "Source variant: $($Latest.Name)"
Write-Host "Files restored: $Copied"
Write-Host "Files skipped:  $Skipped"
Write-Host "Copy failures:  $Failed"
Write-Host "Backup: $Backup"
Write-Host "Report: $Log"

# 5. Compile Java and capture all compiler errors
Write-Host ""
Write-Host "========== GRADLE COMPILE ==========" -ForegroundColor Yellow

$Gradle = Join-Path $Repo "gradlew.bat"
if (!(Test-Path -LiteralPath $Gradle)) { throw "gradlew.bat not found" }

$ErrorActionPreference = "Continue"

& $Gradle compileJava --no-daemon 2>&1 |
    Tee-Object -FilePath $BuildLog

$BuildExitCode = $LASTEXITCODE

# 6. Extract compilation errors into a separate report
$ErrorsLog = Join-Path $Report "java-errors.txt"

$Errors = @(
    Get-Content -LiteralPath $BuildLog |
        Select-String -Pattern '(?i)(\.java:\d+: error:|error:|symbol:|location:|cannot find symbol|package .* does not exist|Compilation failed)' |
        ForEach-Object { $_.Line }
)

if ($Errors.Count -gt 0) {
    $Errors | Set-Content -LiteralPath $ErrorsLog -Encoding UTF8
}

Write-Host ""
Write-Host "========== FINAL RESULT ==========" -ForegroundColor Cyan

if ($BuildExitCode -eq 0) {
    Write-Host "BUILD SUCCESS" -ForegroundColor Green
}
else {
    Write-Host "BUILD FAILED (exit code $BuildExitCode)" -ForegroundColor Red
}

Write-Host "Restored: $Copied"
Write-Host "Copy failures: $Failed"
Write-Host "Java diagnostic lines: $($Errors.Count)"
Write-Host "Full build log: $BuildLog"
Write-Host "Java errors: $ErrorsLog"
Write-Host "Original files backup: $Backup"

if ($Errors.Count -gt 0) {
    Write-Host ""
    Write-Host "========== JAVA ERRORS ==========" -ForegroundColor Red
    $Errors | Select-Object -First 100 | ForEach-Object { Write-Host $_ }
}

exit $BuildExitCode