
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ExpectedBranch = "fix/event-bus-register-crash"
$CommitMessage = "fix: remove duplicated DeferredRegister event bus registration"

function Fail([string]$Message) {
    Write-Host "[ERROR] $Message" -ForegroundColor Red
    exit 1
}

function Info([string]$Message) {
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Invoke-Git([string[]]$Arguments) {
    $output = & git @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        Fail "Git failed: git $($Arguments -join ' ')`n$output"
    }
    return $output
}

# Find repository root.
$Root = (& git rev-parse --show-toplevel 2>$null)
if ($LASTEXITCODE -ne 0 -or !$Root) {
    Fail "Not inside a Git repository."
}
$Root = ($Root | Select-Object -First 1).Trim()
Set-Location $Root

# Never modify the wrong branch.
$Branch = (& git branch --show-current).Trim()
if ($Branch -ne $ExpectedBranch) {
    Fail "Wrong branch: $Branch. Expected: $ExpectedBranch"
}

# Avoid mixing unrelated changes into an automatic commit.
$Status = @(git status --porcelain --untracked-files=normal)
if ($Status.Count -gt 0) {
    Fail "Working tree is not clean. Commit or stash changes first."
}

$SourceDir = Join-Path $Root "src/main/java"
$MainFile = Join-Path $SourceDir "org/slavicmyths/SlavicMyths.java"
$Gradle = Join-Path $Root "gradlew.bat"

if (!(Test-Path $SourceDir)) {
    Fail "Java source directory missing."
}
if (!(Test-Path $MainFile)) {
    Fail "SlavicMyths.java not found. Refusing unsafe edits."
}
if (!(Test-Path $Gradle)) {
    Fail "gradlew.bat not found."
}

Info "Scanning Java sources..."

$Files = @(Get-ChildItem $SourceDir -Recurse -File -Filter "*.java")
$Utf8 = New-Object System.Text.UTF8Encoding($false)
$Pattern = '(?m)^(?<indent>[ \t]*)(?<statement>(?<owner>[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\.register\(\s*(?:bus|modEventBus)\s*\)\s*;)[ \t]*(?://[^\r\n]*)?$'

$Entries = New-Object System.Collections.Generic.List[object]
$Owners = @{}

foreach ($File in $Files) {
    $Content = [System.IO.File]::ReadAllText($File.FullName)
    $Matches = [regex]::Matches($Content, $Pattern)

    foreach ($Match in $Matches) {
        $Owner = $Match.Groups["owner"].Value
        $Parts = $Owner.Split('.')
        $Name = $Parts[$Parts.Length - 1]

        $Entry = [pscustomobject]@{
            File = $File.FullName
            Owner = $Owner
            Name = $Name
            Text = $Match.Value
            Index = $Match.Index
        }
        $Entries.Add($Entry)

        if (!$Owners.ContainsKey($Name)) {
            $Owners[$Name] = New-Object System.Collections.Generic.List[object]
        }
        $Owners[$Name].Add($Entry)
    }
}

Info "Found $($Entries.Count) register(bus/modEventBus) calls."

# Only process known DeferredRegister fields.
# A matching declaration must exist in a Java source file.
$DeclarationPattern = '(?m)\b(?:DeferredRegister|DeferredHolder)\s*(?:<[^;\r\n]*>)?\s+([A-Za-z_$][\w$]*)\s*(?:=|;)'
$KnownRegisters = @{}

foreach ($File in $Files) {
    $Content = [System.IO.File]::ReadAllText($File.FullName)
    foreach ($Match in [regex]::Matches($Content, $DeclarationPattern)) {
        $KnownRegisters[$Match.Groups[1].Value] = $true
    }
}

# Main mod class is authoritative.
$MainText = [System.IO.File]::ReadAllText($MainFile)
$MainNames = @{}

foreach ($Entry in $Entries) {
    if ($Entry.File -eq $MainFile.FullName) {
        $MainNames[$Entry.Name] = $true
    }
}

# SlavicMyths.java may use other local event-bus names.
foreach ($Name in $KnownRegisters.Keys) {
    $Escaped = [regex]::Escape($Name)
    $MainPattern = "\b$Escaped\.register\(\s*[A-Za-z_$][\w$]*\s*\)\s*;"
    if ([regex]::IsMatch($MainText, $MainPattern)) {
        $MainNames[$Name] = $true
    }
}

$RemovalMap = @{}
$Removed = 0
$Skipped = 0

foreach ($Entry in $Entries) {
    # Do not modify registrations in the main class.
    if ($Entry.File -eq $MainFile) {
        continue
    }

    # Avoid removing unrelated register(bus) methods.
    if (!$KnownRegisters.ContainsKey($Entry.Name)) {
        $Skipped++
        continue
    }

    # Never remove a registration unless the main class
    # visibly owns that same registrar.
    if (!$MainNames.ContainsKey($Entry.Name)) {
        $Skipped++
        continue
    }

    # Avoid treating unrelated fields with identical names
    # as the same registrar.
    if (!$Owners.ContainsKey($Entry.Name)) {
        continue
    }

    if (!$RemovalMap.ContainsKey($Entry.File)) {
        $RemovalMap[$Entry.File] = New-Object System.Collections.Generic.List[object]
    }

    $RemovalMap[$Entry.File].Add($Entry)
}

$ChangedFiles = New-Object System.Collections.Generic.List[string]

foreach ($Path in $RemovalMap.Keys) {
    $Content = [System.IO.File]::ReadAllText($Path)
    $FileEntries = @($RemovalMap[$Path] | Sort-Object Index -Descending)

    foreach ($Entry in $FileEntries) {
        $Original = $Entry.Text

        # Preserve the original Java statement as a comment.
        $Replacement = $Original -replace '^([ \t]*)', '$1// AUTO-FIX duplicate: '

        $Content = $Content.Remove($Entry.Index, $Original.Length).Insert($Entry.Index, $Replacement)

        $Removed++
        Info "Disabled: $($Entry.Name) in $([IO.Path]::GetFileName($Path))"
    }

    [System.IO.File]::WriteAllText($Path, $Content, $Utf8)
    $ChangedFiles.Add($Path)
}

Info "Disabled registrations: $Removed"
Info "Skipped ambiguous calls: $Skipped"

if ($Removed -eq 0) {
    Info "No safely identifiable duplicate registrations found."
    exit 0
}

# Compile after changes.
Info "Running Gradle compileJava..."

& $Gradle compileJava --no-daemon
$BuildExitCode = $LASTEXITCODE

if ($BuildExitCode -ne 0) {
    Write-Host "[FAIL] Compilation failed. No commit created." -ForegroundColor Red
    Write-Host "Review the diff with: git diff" -ForegroundColor Yellow
    exit $BuildExitCode
}

Write-Host "[PASS] Java compilation successful." -ForegroundColor Green

# Stage only files modified by this script.
foreach ($Path in $ChangedFiles) {
    & git add -- $Path
    if ($LASTEXITCODE -ne 0) {
        Fail "Unable to stage: $Path"
    }
}

$Staged = @(git diff --cached --name-only)
if ($Staged.Count -eq 0) {
    Info "No staged changes."
    exit 0
}

Info "Creating Git commit..."

& git commit -m $CommitMessage
if ($LASTEXITCODE -ne 0) {
    Fail "Git commit failed."
}

$Sha = (& git rev-parse HEAD).Trim()

Write-Host ""
Write-Host "====================================" -ForegroundColor Green
Write-Host "AUTO-FIX COMPLETED" -ForegroundColor Green
Write-Host "Branch: $Branch"
Write-Host "Disabled calls: $Removed"
Write-Host "Files modified: $($ChangedFiles.Count)"
Write-Host "Compilation: PASS"
Write-Host "Commit: $Sha"
Write-Host "====================================" -ForegroundColor Green
