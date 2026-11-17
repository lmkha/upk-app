@echo off
rem Copies the files of one environment from its folder in secrets\ to the
rem places the build reads them from. secrets\ is git-ignored and handed out
rem by the project owner.
rem
rem Usage, with dev, staging or prod:
rem   scripts\use-env.bat dev
rem
rem macOS and Linux use use-env.sh. Keep the two scripts in sync.

setlocal

cd /d "%~dp0.."

if not exist "secrets\" goto :no_secrets_folder

set "ENVIRONMENT_NAME=%~1"
if "%ENVIRONMENT_NAME%"=="" goto :usage
if not "%ENVIRONMENT_NAME:\=%"=="%ENVIRONMENT_NAME%" goto :usage
if not "%ENVIRONMENT_NAME:/=%"=="%ENVIRONMENT_NAME%" goto :usage
if "%ENVIRONMENT_NAME:~0,1%"=="." goto :usage
set "SOURCE_DIR=secrets\%ENVIRONMENT_NAME%"
if not exist "%SOURCE_DIR%\" goto :usage

rem Files copied from the environment folder. When the setup needs another file, add
rem it to this list, to the remove list and the copy lines below, and to use-env.sh.
rem Check everything before copying anything, so a failure never leaves the files
rem of two environments mixed.
for %%F in (secrets.properties google-services.json) do (
    if not exist "%SOURCE_DIR%\%%F" (
        echo error: %SOURCE_DIR%\%%F not found. 1>&2
        exit /b 1
    )
)

rem Remove the files of the previous environment before writing the new ones. If a
rem copy then fails, a file is missing and the build stops, instead of running on
rem the files of two environments.
for %%F in ("secrets.properties" "app\google-services.json") do (
    if exist %%F del /f /q %%F
    if exist %%F (
        echo error: cannot remove %%~F. 1>&2
        exit /b 1
    )
)

copy /y "%SOURCE_DIR%\secrets.properties" "secrets.properties" >nul || exit /b 1
copy /y "%SOURCE_DIR%\google-services.json" "app\google-services.json" >nul || exit /b 1

echo Environment set to '%ENVIRONMENT_NAME%'. Sync Gradle in Android Studio before the next build.
exit /b 0

:no_secrets_folder
echo error: secrets\ not found. Ask the project owner for it and put it in the repository root. 1>&2
exit /b 1

:usage
echo error: usage: scripts\use-env.bat ^<environment^>. Available in secrets\: 1>&2
dir /b /ad "secrets" 1>&2
exit /b 1
