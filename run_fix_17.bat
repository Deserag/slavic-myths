@echo off
setlocal
cd /d "%~dp0"
where py >nul 2>nul
if errorlevel 1 goto use_python
py -3 "%~dp0fix_slavic_myths_17.py" --root "%CD%"
goto compile
:use_python
where python >nul 2>nul
if errorlevel 1 (
    echo Python 3 is required to run fix_slavic_myths_17.py
    exit /b 1
)
python "%~dp0fix_slavic_myths_17.py" --root "%CD%"
:compile
if errorlevel 1 exit /b 1
call gradlew.bat compileJava --no-daemon
exit /b %ERRORLEVEL%
