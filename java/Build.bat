@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Build.ps1"
if errorlevel 1 (
  echo Build or tests failed.
  pause
  exit /b 1
)
echo Finished. Your Minecraft mod is in dist\slime-gun-1.0.0.jar
pause
