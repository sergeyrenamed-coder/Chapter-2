@echo off
setlocal EnableExtensions EnableDelayedExpansion
set GRADLE_VERSION=8.8
where gradle >nul 2>nul
if not errorlevel 1 (call gradle %* & exit /b !ERRORLEVEL!)
set CACHE_ROOT=%USERPROFILE%\.gradle\cursedquest-gradle
set GRADLE_HOME=%CACHE_ROOT%\gradle-%GRADLE_VERSION%
if exist "%GRADLE_HOME%\bin\gradle.bat" goto runGradle
if not exist "%CACHE_ROOT%" mkdir "%CACHE_ROOT%"
set ARCHIVE=%CACHE_ROOT%\gradle-%GRADLE_VERSION%-bin.zip
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ARCHIVE%'; Expand-Archive -LiteralPath '%ARCHIVE%' -DestinationPath '%CACHE_ROOT%' -Force; Remove-Item -LiteralPath '%ARCHIVE%' -Force"
if errorlevel 1 exit /b !ERRORLEVEL!
:runGradle
call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b !ERRORLEVEL!
