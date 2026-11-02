
# smart-git-patch.ps1
# PowerShell 5.1 / 7+
# NeoForge 1.21.1 - DeferredRegister duplicate registration repair

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ExpectedBranch = "fix/event-bus-register-crash"
$CommitMessage = "fix: autonomously resolved duplicated event bus registration for DeferredRegister"
$Utf8 = New-Object System.Text.UTF8Encoding($false)

function Fail([string]$Message) {
    Write-Host "[ERROR] $Message" -ForegroundColor Red
    exit 1
}

function Info([string]$Message) {
    Write-Host "[INFO] $Message" -ForegroundColor Cyan
}

function Success([string]$Message) {
    Write-Host "[PASS] $Message" -ForegroundColor Green
}

function Get-RelativePath([string]$Path) {
    return $Path.Substring($Root.Length).TrimStart('\', '/').Replace('\', '/')
}

function Get-CodeWithoutComments([string]$Source) {
    # Preserve offsets and line breaks while masking comments and strings.
    $Chars = $Source.ToCharArray()
    $Output = $Source.ToCharArray()
    $State = "code"
    $i = 0

    while ($i -lt $Chars.Length) {
        $c = $Chars[$i]
        $n = [char]0
        if ($i + 1 -lt $Chars.Length) {
            $n = $Chars[$i + 1]
        }

        if ($State -eq "code") {
            if ($c -eq '/' -and $n -eq '/') {
                $Output[$i] = ' '
                $Output[$i + 1] = ' '
                $i += 2
                $State = "line"
                continue
            }

            if ($c -eq '/' -and $n -eq '*') {
                $Output[$i] = ' '
                $Output[$i + 1] = ' '
                $i += 2
                $State = "block"
                continue
            }

            if ($c -eq '"') {
                $Output[$i] = ' '
                $State = "string"
                $i++
                continue
            }

            if ($c -eq "'") {
                $Output[$i] = ' '
                $State = "char"
                $i++
                continue
            }
        }
        elseif ($State -eq "line") {
            if ($c -eq "`n" -or $c -eq "`r") {
                $State = "code"
            }
            else {
                $Output[$i] = ' '
            }
        }
        elseif ($State -eq "block") {
            if ($c -eq '*' -and $n -eq '/') {
                $Output[$i] = ' '
                $Output[$i + 1] = ' '
                $i += 2
                $State = "code"
                continue
            }

            if ($c -ne "`n" -and $c -ne "`r") {
                $Output[$i] = ' '
            }
        }
        elseif ($State -eq "string" -or $State -eq "char") {
            $EndChar = '"'
            if ($State -eq "char") {
                $EndChar = "'"
            }

            if ($c -eq '\') {
                $Output[$i] = ' '
                if ($i + 1 -lt $Chars.Length) {
                    $Output[$i + 1] = ' '
                }
                $i += 2
                continue
            }

            if ($c -eq $EndChar) {
                $State = "code"
            }

            if ($c -ne "`n" -and $c -ne "`r") {
                $Output[$i] = ' '
            }
        }

        $i++
    }

    return -join $Output
}

function Get-Package([string]$Code) {
    $m = [regex]::Match($Code, '(?m)^\s*package\s+([\w.]+)\s*;')
    if ($m.Success) {
        return $m.Groups[1].Value
    }
    return ""
}

function Get-ClassName([string]$Code, [string]$Fallback) {
    $m = [regex]::Match($Code, '\b(?:class|record|enum)\s+([A-Za-z_$][\w$]*)')
    if ($m.Success) {
        return $m.Groups[1].Value
    }
    return $Fallback
}

# ------------------------------------------------------------
# 1. Repository validation
# ------------------------------------------------------------

$RootResult = & git rev-parse --show-toplevel 2>$null
if ($LASTEXITCODE -ne 0 -or !$RootResult) {
    Fail "Git repository not found."
}

$Root = ([string]($RootResult | Select-Object -First 1)).Trim()
Set-Location $Root

$Branch = (& git branch --show-current).Trim()
if ($Branch -ne $ExpectedBranch) {
    Fail "Wrong branch '$Branch'. Expected '$ExpectedBranch'."
}

$Status = @(git status --porcelain --untracked-files=normal)
if ($Status.Count -gt 0) {
    Fail "Working tree is not clean. Commit or stash existing changes first."
}

$SourceDir = Join-Path $Root "src/main/java/org/slavicmyths"
$MainFile = Join-Path $SourceDir "SlavicMyths.java"
$Gradle = Join-Path $Root "gradlew.bat"

if (!(Test-Path $SourceDir)) {
    Fail "Source directory missing."
}
if (!(Test-Path $MainFile)) {
    Fail "SlavicMyths.java missing."
}
if (!(Test-Path $Gradle)) {
    Fail "gradlew.bat missing."
}

Info "Branch: $Branch"
Info "Scanning DeferredRegister declarations and register calls..."

# ------------------------------------------------------------
# 2. Parse Java files
# ------------------------------------------------------------

$Files = @(Get-ChildItem $SourceDir -Recurse -File -Filter "*.java")
$ParsedFiles = @{}
$Declarations = @{}
$Calls = New-Object System.Collections.Generic.List[object]

foreach ($File in $Files) {
    $Source = [IO.File]::ReadAllText($File.FullName)
    $Code = Get-CodeWithoutComments $Source
    $Package = Get-Package $Code
    $ClassName = Get-ClassName $Code $File.BaseName
    $FullClass = "$Package.$ClassName"

    $ParsedFiles[$File.FullName] = [pscustomobject]@{
        Source = $Source
        Code = $Code
        Package = $Package
        ClassName = $ClassName
        FullClass = $FullClass
    }

    # Detect fields declared as DeferredRegister or its nested types.
    $DeclarationPattern = '\b(?:DeferredRegister(?:\.[A-Za-z_$][\w$]*)?)\s*(?:<[^;=\r\n]+>)?\s+([A-Za-z_$][\w$]*)\s*='

    foreach ($Match in [regex]::Matches($Code, $DeclarationPattern)) {
        $Field = $Match.Groups[1].Value
        $Key = "$FullClass#$Field"

        $Declarations[$Key] = [pscustomobject]@{
            Key = $Key
            Field = $Field
            Class = $ClassName
            FullClass = $FullClass
            File = $File.FullName
        }
    }

    # Any event bus variable name is accepted.
    # Only standalone statements are candidates for modification.
    $CallPattern = '(?m)^(?<indent>[ \t]*)(?<owner>[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\s*\.register\s*\(\s*(?<bus>[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\s*\)\s*;[ \t]*$'

    foreach ($Match in [regex]::Matches($Code, $CallPattern)) {
        $Owner = $Match.Groups["owner"].Value
        $Bus = $Match.Groups["bus"].Value

        $Before = $Source.Substring(0, $Match.Index)
        $LineNumber = ([regex]::Matches($Before, "`n")).Count + 1

        $Calls.Add([pscustomobject]@{
            File = $File.FullName
            Class = $ClassName
            FullClass = $FullClass
            Owner = $Owner
            Bus = $Bus
            Index = $Match.Index
            Length = $Match.Length
            Line = $LineNumber
            Key = ""
        })
    }
}

Info "Java files: $($Files.Count)"
Info "DeferredRegister declarations: $($Declarations.Count)"
Info "Standalone register calls: $($Calls.Count)"

# ------------------------------------------------------------
# 3. Resolve register owners to concrete declarations
# ------------------------------------------------------------

$ResolvedCalls = New-Object System.Collections.Generic.List[object]
$UnresolvedCalls = New-Object System.Collections.Generic.List[object]

foreach ($Call in $Calls) {
    $Parts = $Call.Owner.Split('.')
    $FieldName = $Parts[$Parts.Length - 1]
    $Candidates = New-Object System.Collections.Generic.List[string]

    if ($Parts.Length -eq 1) {
        $LocalKey = "$($Call.FullClass)#$FieldName"

        if ($Declarations.ContainsKey($LocalKey)) {
            $Candidates.Add($LocalKey)
        }

        $Parsed = $ParsedFiles[$Call.File]
        $ImportPattern = '(?m)^\s*import\s+static\s+([\w.]+)\.' + [regex]::Escape($FieldName) + '\s*;'

        foreach ($Import in [regex]::Matches($Parsed.Code, $ImportPattern)) {
            $Key = "$($Import.Groups[1].Value)#$FieldName"
            if ($Declarations.ContainsKey($Key)) {
                $Candidates.Add($Key)
            }
        }

        $WildcardPattern = '(?m)^\s*import\s+static\s+([\w.]+)\.\*\s*;'

        foreach ($Import in [regex]::Matches($Parsed.Code, $WildcardPattern)) {
            $Key = "$($Import.Groups[1].Value)#$FieldName"
            if ($Declarations.ContainsKey($Key)) {
                $Candidates.Add($Key)
            }
        }
    }
    else {
        $OwnerClass = ($Parts[0..($Parts.Length - 2)] -join '.')
        $Parsed = $ParsedFiles[$Call.File]

        foreach ($Decl in $Declarations.Values) {
            if ($Decl.Field -ne $FieldName) {
                continue
            }

            if ($OwnerClass -eq $Decl.FullClass) {
                $Candidates.Add($Decl.Key)
                continue
            }

            if ($OwnerClass -eq $Decl.Class) {
                $ImportPattern = '(?m)^\s*import\s+' + [regex]::Escape($Decl.FullClass) + '\s*;'
                $SamePackage = ($Parsed.Package -eq ($Decl.FullClass -replace '\.[^.]+$', ''))

                if ($SamePackage -or [regex]::IsMatch($Parsed.Code, $ImportPattern)) {
                    $Candidates.Add($Decl.Key)
                }
            }
        }
    }

    $UniqueCandidates = @($Candidates | Select-Object -Unique)

    if ($UniqueCandidates.Count -eq 1) {
        $Call.Key = $UniqueCandidates[0]
        $ResolvedCalls.Add($Call)
    }
    else {
        $UnresolvedCalls.Add($Call)
    }
}

Info "Resolved register calls: $($ResolvedCalls.Count)"
Info "Unresolved register calls: $($UnresolvedCalls.Count)"

# ------------------------------------------------------------
# 4. Detect duplicate registrations
# ------------------------------------------------------------

$Groups = @($ResolvedCalls | Group-Object Key)
$Fixes = New-Object System.Collections.Generic.List[object]
$AmbiguousGroups = New-Object System.Collections.Generic.List[string]

foreach ($Group in $Groups) {
    $GroupCalls = @($Group.Group)

    if ($GroupCalls.Count -le 1) {
        continue
    }

    $Declaration = $Declarations[$Group.Name]

    Info "Duplicate candidate: $($Declaration.Key) ($($GroupCalls.Count) calls)"

    $MainCalls = @($GroupCalls | Where-Object { $_.File -eq $MainFile })
    $OtherCalls = @($GroupCalls | Where-Object { $_.File -ne $MainFile })

    # The main class is authoritative only when the declaration
    # and the duplicate registrations resolve to the same object.
    if ($MainCalls.Count -eq 1) {
        foreach ($Call in $OtherCalls) {
            $Fixes.Add($Call)
        }

        foreach ($Call in $GroupCalls) {
            Info "  $((Get-RelativePath $Call.File)):$($Call.Line)"
        }
    }
    else {
        $AmbiguousGroups.Add($Group.Name)
        Info "  Skipped: no unique main-class registration."
    }
}

# Do not silently claim a complete repair if resolution is ambiguous.
if ($AmbiguousGroups.Count -gt 0) {
    foreach ($Key in $AmbiguousGroups) {
        Write-Host "[WARN] Manual resolution needed: $Key" -ForegroundColor Yellow
    }
}

# ------------------------------------------------------------
# 5. Apply changes
# ------------------------------------------------------------

$ChangedFiles = New-Object System.Collections.Generic.List[string]
$FixesByFile = @($Fixes | Group-Object File)

foreach ($FileGroup in $FixesByFile) {
    $Path = $FileGroup.Name
    $Original = $ParsedFiles[$Path].Source
    $Content = $Original

    $SortedFixes = @($FileGroup.Group | Sort-Object Index -Descending)

    foreach ($Fix in $SortedFixes) {
        $Statement = $Content.Substring($Fix.Index, $Fix.Length)
        $Replacement = [regex]::Replace($Statement, '^([ \t]*)', '$1// AUTO-FIX: duplicate DeferredRegister registration: ')

        $Content = $Content.Remove($Fix.Index, $Fix.Length).Insert($Fix.Index, $Replacement)

        Info "Disabled: $((Get-RelativePath $Path)):$($Fix.Line) => $($Fix.Owner).register($($Fix.Bus))"
    }

    if ($Content -ne $Original) {
        [IO.File]::WriteAllText($Path, $Content, $Utf8)
        $ChangedFiles.Add($Path)
    }
}

Info "Duplicate calls disabled: $($Fixes.Count)"
Info "Modified files: $($ChangedFiles.Count)"

if ($ChangedFiles.Count -eq 0) {
    Write-Host "[WARN] No unambiguous duplicate registrations were modified." -ForegroundColor Yellow
    exit 2
}

# ------------------------------------------------------------
# 6. Forced compilation
# ------------------------------------------------------------

Info "Running compileJava --no-daemon --rerun-tasks..."

& $Gradle compileJava --no-daemon --rerun-tasks
$CompileExit = $LASTEXITCODE

if ($CompileExit -ne 0) {
    Fail "Java compilation failed. Changes remain uncommitted for inspection."
}

Success "Java compilation successful."

# ------------------------------------------------------------
# 7. Runtime smoke test
# ------------------------------------------------------------

# A successful compileJava does not prove successful mod loading.
# RunClient may keep running until Minecraft is closed.
# The smoke test uses a bounded timeout and inspects the runtime log.

Info "Starting Minecraft runtime smoke test..."

$RuntimeLog = Join-Path $Root "run/logs/latest.log"
$CrashDir = Join-Path $Root "run/crash-reports"

$StartTime = Get-Date
$TimeoutSeconds = 120
$RuntimeFailed = $false
$RuntimeReady = $false

$Process = Start-Process -FilePath $Gradle -ArgumentList @("runClient", "--no-daemon") -WorkingDirectory $Root -PassThru -WindowStyle Hidden

try {
    while (((Get-Date) - $StartTime).TotalSeconds -lt $TimeoutSeconds) {
        Start-Sleep -Seconds 3
        $Process.Refresh()

        if (Test-Path $RuntimeLog) {
            $Log = Get-Content $RuntimeLog -Raw -ErrorAction SilentlyContinue

            if ($Log -match 'Cannot register DeferredRegister to more than one event bus|Failed to create mod instance|ModLoadingCrashException') {
                $RuntimeFailed = $true
                break
            }

            if ($Log -match 'Minecraft.*Stopping!') {
                break
            }

            if ($Log -match 'OpenAL initialized|Sound engine started') {
                # Not sufficient alone: verify no startup failure.
                if ($Log -notmatch 'Failed to create mod instance|ModLoadingCrashException|Cannot register DeferredRegister') {
                    $RuntimeReady = $true
                }
            }
        }

        if ($Process.HasExited) {
            break
        }
    }
}
finally {
    $Process.Refresh()

    if (!$Process.HasExited) {
        # The launched Gradle process is stopped after the smoke test.
        # Its Java child may require separate cleanup by the operator.
        Stop-Process -Id $Process.Id -Force -ErrorAction SilentlyContinue
    }
}

if ($RuntimeFailed) {
    Fail "Minecraft mod initialization failed again. No commit created."
}

# Runtime readiness is not proven by a partial log.
if (!$RuntimeReady) {
    Write-Host "[WARN] Runtime verification inconclusive. No commit created." -ForegroundColor Yellow
    exit 3
}

# Ensure no later crash was written during the smoke test.
if (Test-Path $CrashDir) {
    $NewCrashes = @(Get-ChildItem $CrashDir -Filter "*.txt" -File | Where-Object { $_.LastWriteTime -ge $StartTime })

    if ($NewCrashes.Count -gt 0) {
        Fail "New Minecraft crash report detected. No commit created."
    }
}

Success "Runtime smoke test passed its log checks."

# ------------------------------------------------------------
# 8. Git commit
# ------------------------------------------------------------

Info "Staging modified files..."

# Requested staging command, safe because working tree was clean.
& git add .
if ($LASTEXITCODE -ne 0) {
    Fail "git add failed."
}

$StagedFiles = @(git diff --cached --name-only)

if ($StagedFiles.Count -eq 0) {
    Fail "No staged changes."
}

Info "Creating commit..."

& git commit -m $CommitMessage
if ($LASTEXITCODE -ne 0) {
    Fail "git commit failed."
}

$Sha = (& git rev-parse HEAD).Trim()

Write-Host ""
Write-Host "===============================================" -ForegroundColor Green
Write-Host "SMART GIT PATCH COMPLETED" -ForegroundColor Green
Write-Host "Branch: $Branch"
Write-Host "DeferredRegister objects: $($Declarations.Count)"
Write-Host "Resolved calls: $($ResolvedCalls.Count)"
Write-Host "Removed duplicates: $($Fixes.Count)"
Write-Host "Modified files: $($ChangedFiles.Count)"
Write-Host "Compile: PASS"
Write-Host "Runtime log checks: PASS"
Write-Host "Commit SHA: $Sha"
Write-Host "===============================================" -ForegroundColor Green
