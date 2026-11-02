# fix-jvm-compile.ps1
# Minecraft 1.21.1 / NeoForge 21.1.255
# Запуск: powershell -ExecutionPolicy Bypass -File .\fix-jvm-compile.ps1

$ErrorActionPreference = "Stop"

# 1. Определение корня репозитория
$Repo = (& git rev-parse --show-toplevel).Trim()
if ($LASTEXITCODE -ne 0 -or !$Repo) {
    throw "ERROR: Git repository not found"
}

Set-Location $Repo

$OldJavaHome = $env:JAVA_HOME
$OldPath = $env:Path

$script:Candidates = @()
$script:Seen = @{}

# 2. Проверка установленной JDK
function Add-JdkCandidate {
    param([string]$Home)

    if ([string]::IsNullOrWhiteSpace($Home)) { return }

    $Home = [Environment]::ExpandEnvironmentVariables($Home.Trim('"'))

    if ($Home -match '(?i)[\\/]bin[\\/]java(c)?\.exe$') {
        $Home = Split-Path (Split-Path $Home -Parent) -Parent
    }

    if (!(Test-Path -LiteralPath $Home -PathType Container)) { return }

    $Home = (Resolve-Path -LiteralPath $Home).Path

    if ($script:Seen.ContainsKey($Home)) { return }
    $script:Seen[$Home] = $true

    $Java = Join-Path $Home "bin\java.exe"
    $Javac = Join-Path $Home "bin\javac.exe"

    # Для компиляции нужна полноценная JDK, а не только JRE.
    if (!(Test-Path -LiteralPath $Java)) { return }
    if (!(Test-Path -LiteralPath $Javac)) { return }

    try {
        $Psi = New-Object System.Diagnostics.ProcessStartInfo
        $Psi.FileName = $Java
        $Psi.Arguments = "-version"
        $Psi.UseShellExecute = $false
        $Psi.RedirectStandardError = $true
        $Psi.RedirectStandardOutput = $true

        $Process = [System.Diagnostics.Process]::Start($Psi)
        $VersionText = $Process.StandardError.ReadToEnd() + $Process.StandardOutput.ReadToEnd()
        $Process.WaitForExit()
        $Process.Dispose()

        $Match = [regex]::Match($VersionText, '(?i)(?:openjdk\s+version|java\s+version|openjdk)\s+"?(\d+)')

        if (!$Match.Success) { return }

        $Major = [int]$Match.Groups[1].Value

        if ($Major -eq 17 -or $Major -eq 21) {
            $script:Candidates += [PSCustomObject]@{
                Home = $Home
                Major = $Major
            }

            Write-Host "FOUND: Java $Major -> $Home" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "SKIP: $Home" -ForegroundColor DarkYellow
    }
}

Write-Host "`n========== SEARCH JAVA ==========" -ForegroundColor Cyan

# 3. Проверка JAVA_HOME
Add-JdkCandidate $env:JAVA_HOME

# 4. Стандартные директории Windows
$Patterns = @(
    "C:\Program Files\Java\jdk-21*",
    "C:\Program Files\Java\jdk-17*",
    "C:\Program Files\Eclipse Foundation\jdk-21*",
    "C:\Program Files\Eclipse Foundation\jdk-17*",
    "C:\Program Files\Eclipse Adoptium\jdk-21*",
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\Microsoft\jdk-21*",
    "C:\Program Files\Microsoft\jdk-17*",
    "C:\Program Files\Amazon Corretto\jdk21*",
    "C:\Program Files\Amazon Corretto\jdk17*",
    "$env:USERPROFILE\.jdks\*",
    "$env:USERPROFILE\.gradle\jdks\*",
    "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\*"
)

foreach ($Pattern in $Patterns) {
    Get-ChildItem -Path $Pattern -Directory -ErrorAction SilentlyContinue |
        ForEach-Object { Add-JdkCandidate $_.FullName }
}

# 5. Проверка Java в PATH
Get-Command java.exe, javac.exe -All -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandType -eq "Application" } |
    ForEach-Object { Add-JdkCandidate $_.Source }

# 6. Проверка настроек Java в VS Code
$SettingsFiles = @(
    "$env:APPDATA\Code\User\settings.json",
    "$env:APPDATA\Code - Insiders\User\settings.json"
)

foreach ($SettingsFile in $SettingsFiles) {
    if (!(Test-Path -LiteralPath $SettingsFile)) { continue }

    $SettingsText = Get-Content -LiteralPath $SettingsFile -Raw

    $Matches = [regex]::Matches(
        $SettingsText,
        '"(?:java\.home|java\.jdt\.ls\.java\.home|path)"\s*:\s*("(?:\\.|[^"\\])*")'
    )

    foreach ($Entry in $Matches) {
        try {
            $CandidatePath = $Entry.Groups[1].Value | ConvertFrom-Json
            Add-JdkCandidate $CandidatePath
        }
        catch {}
    }
}

# 7. Проверка VS Code Extensions и Minecraft Launcher
$RuntimeRoots = @(
    "$env:USERPROFILE\.vscode\extensions",
    "$env:USERPROFILE\.vscode-insiders\extensions",
    "$env:APPDATA\.minecraft\runtime"
)

foreach ($RuntimeRoot in $RuntimeRoots) {
    if (!(Test-Path -LiteralPath $RuntimeRoot)) { continue }

    $SearchRoot = $RuntimeRoot

    if ($RuntimeRoot -match '(?i)extensions$') {
        $ExtensionDirs = Get-ChildItem -LiteralPath $RuntimeRoot -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match '(?i)(java|jdk|redhat)' }

        foreach ($ExtensionDir in $ExtensionDirs) {
            Get-ChildItem -LiteralPath $ExtensionDir.FullName -Filter "javac.exe" -File -Recurse -ErrorAction SilentlyContinue |
                ForEach-Object {
                    Add-JdkCandidate (Split-Path (Split-Path $_.FullName -Parent) -Parent)
                }
        }
    }
    else {
        Get-ChildItem -LiteralPath $SearchRoot -Filter "java.exe" -File -Recurse -ErrorAction SilentlyContinue |
            ForEach-Object {
                Add-JdkCandidate (Split-Path (Split-Path $_.FullName -Parent) -Parent)
            }
    }
}

# 8. Выбор JDK 21 (обязательна для Minecraft 1.21.1)
$SelectedJdk = $script:Candidates |
    Where-Object { $_.Major -eq 21 } |
    Select-Object -First 1

if (!$SelectedJdk) {
    Write-Host "`nERROR: JDK 21 NOT FOUND" -ForegroundColor Red
    Write-Host "Minecraft 1.21.1 requires Java 21. Java 17 is insufficient."
    Write-Host "Install JDK 21 and run this script again."
    exit 1
}

$JavaHome = $SelectedJdk.Home
$GradleJavaHome = $JavaHome.Replace('\', '/')

Write-Host "`n========== SELECTED JDK ==========" -ForegroundColor Cyan
Write-Host "JAVA_HOME = $JavaHome" -ForegroundColor Green

try {
    # 9. Временная смена Java только для этого процесса
    $env:JAVA_HOME = $JavaHome
    $env:Path = "$JavaHome\bin;$OldPath"

    Write-Host "`n========== JAVA VERSION ==========" -ForegroundColor Cyan
    & "$JavaHome\bin\java.exe" -version
    & "$JavaHome\bin\javac.exe" -version

    # 10. Подготовка папки логов внутри Git
    $Stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $ReportDir = (& git rev-parse --git-path "jvm-compile-$Stamp").Trim()

    New-Item -ItemType Directory -Path $ReportDir -Force | Out-Null

    $PropertiesFile = Join-Path $Repo "gradle.properties"
    $PropertiesBackup = Join-Path $ReportDir "gradle.properties.bak"
    $BuildLog = Join-Path $ReportDir "compileJava.log"
    $ErrorsLog = Join-Path $Repo "java-errors.txt"

    # 11. Резервная копия gradle.properties
    if (Test-Path -LiteralPath $PropertiesFile) {
        Copy-Item -LiteralPath $PropertiesFile -Destination $PropertiesBackup -Force
        $PropertiesLines = @(Get-Content -LiteralPath $PropertiesFile -Encoding UTF8)
    }
    else {
        $PropertiesLines = @()
    }

    # 12. Удаление прежних настроек JVM из файла
    $PropertiesLines = @(
        $PropertiesLines | Where-Object {
            $_ -notmatch '^\s*org\.gradle\.java\.home\s*[:=]'
        }
    )

    # 13. Принудительная установка JDK 21
    $PropertiesLines += "org.gradle.java.home=$GradleJavaHome"

    $Utf8 = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($PropertiesFile, [string[]]$PropertiesLines, $Utf8)

    Write-Host "`n========== GRADLE PROPERTIES ==========" -ForegroundColor Cyan
    Write-Host "org.gradle.java.home=$GradleJavaHome" -ForegroundColor Green

    # 14. Остановка старых демонов Gradle
    $Gradle = Join-Path $Repo "gradlew.bat"

    if (!(Test-Path -LiteralPath $Gradle)) {
        throw "ERROR: gradlew.bat not found"
    }

    Write-Host "`n========== STOP GRADLE ==========" -ForegroundColor Yellow

    $ErrorActionPreference = "Continue"
    & $Gradle --stop 2>&1 | Out-Null
    $ErrorActionPreference = "Stop"

    # 15. Очистка локального кеша проекта
    # Глобальный ~/.gradle/caches не удаляем, чтобы не ломать другие проекты.
    $ProjectCache = Join-Path $Repo ".gradle"

    if (Test-Path -LiteralPath $ProjectCache) {
        Write-Host "Removing project Gradle cache..." -ForegroundColor Yellow

        try {
            Remove-Item -LiteralPath $ProjectCache -Recurse -Force
            Write-Host "Project Gradle cache cleared." -ForegroundColor Green
        }
        catch {
            Write-Host "WARNING: Could not fully clear cache: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    # 16. Компиляция с JDK 21
    Write-Host "`n========== COMPILE JAVA 21 ==========" -ForegroundColor Cyan

    $ErrorActionPreference = "Continue"

    & $Gradle clean compileJava --no-daemon --console=plain --no-build-cache --rerun-tasks "-Dorg.gradle.java.home=$GradleJavaHome" 2>&1 |
        ForEach-Object { $_.ToString() } |
        Tee-Object -FilePath $BuildLog

    $BuildExitCode = $LASTEXITCODE
    $ErrorActionPreference = "Stop"

    # 17. Извлечение ошибок компиляции
    $AllLines = @(Get-Content -LiteralPath $BuildLog -Encoding UTF8)

    $ErrorPattern = '(?i)(\.java:\d+(?::\d+)?:\s*error:|^\s*error:|^\s*symbol:|^\s*location:|cannot find symbol|package .* does not exist|incompatible types|> Task .*FAILED|BUILD FAILED|Compilation failed|FAILURE:|What went wrong:)'

    $ErrorIndexes = New-Object 'System.Collections.Generic.HashSet[int]'

    for ($i = 0; $i -lt $AllLines.Count; $i++) {
        if ($AllLines[$i] -match $ErrorPattern) {
            $Start = [Math]::Max(0, $i - 1)
            $End = [Math]::Min($AllLines.Count - 1, $i + 8)

            for ($j = $Start; $j -le $End; $j++) {
                [void]$ErrorIndexes.Add($j)
            }
        }
    }

    $JavaErrors = @(
        $ErrorIndexes |
            Sort-Object |
            ForEach-Object { $AllLines[$_] }
    )

    if ($BuildExitCode -ne 0 -and $JavaErrors.Count -eq 0) {
        $JavaErrors = $AllLines
    }

    if ($BuildExitCode -eq 0) {
        $JavaErrors = @("BUILD SUCCESS - Java compilation completed without errors.")
    }

    [System.IO.File]::WriteAllLines($ErrorsLog, [string[]]$JavaErrors, $Utf8)

    # 18. Финальный отчёт
    Write-Host "`n========== FINAL RESULT ==========" -ForegroundColor Cyan

    if ($BuildExitCode -eq 0) {
        Write-Host "BUILD SUCCESS - JAVA 21" -ForegroundColor Green
    }
    else {
        Write-Host "BUILD FAILED - EXIT CODE $BuildExitCode" -ForegroundColor Red
    }

    Write-Host "JDK: $JavaHome"
    Write-Host "Branch: $((& git branch --show-current).Trim())"
    Write-Host "Java errors: $ErrorsLog"
    Write-Host "Full Gradle log: $BuildLog"
    Write-Host "Gradle backup: $PropertiesBackup"

    if ($BuildExitCode -ne 0) {
        Write-Host "`n========== COMPILATION ERRORS ==========" -ForegroundColor Red
        $JavaErrors | Select-Object -First 150 | ForEach-Object { Write-Host $_ }
    }
}
finally {
    # Восстанавливаем переменные текущей PowerShell-сессии.
    [Environment]::SetEnvironmentVariable("JAVA_HOME", $OldJavaHome, "Process")
    $env:Path = $OldPath
}