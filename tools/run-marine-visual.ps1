param([string]$JavaExecutable,[switch]$Weapons)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
if(!$JavaExecutable){
    $jdk=Get-ChildItem -Path (Join-Path $env:ProgramFiles 'Java') -Directory -Filter 'jdk-21*' |
        Sort-Object Name -Descending | Select-Object -First 1
    if(!$jdk){throw 'Java 21 not found; pass -JavaExecutable with the Gradle toolchain java.exe path.'}
    $JavaExecutable=Join-Path $jdk.FullName 'bin\java.exe'
}
$JavaExecutable=(Resolve-Path -LiteralPath $JavaExecutable).Path
$mutex=[Threading.Mutex]::new($false,'Local\SlavicMythsMarineVisualGpu')
if(!$mutex.WaitOne(0)){$mutex.Dispose();throw 'Another marine visual check is running.'}
$key=$null
try {
    $key=[Microsoft.Win32.Registry]::CurrentUser.CreateSubKey('Software\Microsoft\DirectX\UserGpuPreferences')
    $previous=$key.GetValue($JavaExecutable,$null)
    $key.SetValue($JavaExecutable,'GpuPreference=2;')
    Push-Location -LiteralPath $projectRoot
    try {
        if($Weapons){& .\gradlew.bat runMarineVisual -PmarineVisual -PmarineWeaponVisual --console=plain}
        else {& .\gradlew.bat runMarineVisual -PmarineVisual --console=plain}
        $result=$LASTEXITCODE
    } finally {Pop-Location}
} finally {
    if($key){
        if($null -eq $previous){$key.DeleteValue($JavaExecutable,$false)}else{$key.SetValue($JavaExecutable,$previous)}
        $key.Dispose()
    }
    $mutex.ReleaseMutex();$mutex.Dispose()
}
exit $result