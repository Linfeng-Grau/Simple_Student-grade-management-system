@echo off
rem ============================================================
rem  Score system one-click switch (start / stop)
rem  Double click this file: running -> stop, stopped -> start
rem  All text kept in ASCII on purpose (PowerShell 5.1 codepage).
rem ============================================================
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\toggle-app.ps1" %*
echo.
pause
