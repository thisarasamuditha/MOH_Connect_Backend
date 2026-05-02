@echo off
REM ====================================================
REM MOH Connect Backend - Database Import Script
REM This script imports sample data into the moh_db database
REM ====================================================

echo.
echo ====================================================
echo MOH Connect Backend - Database Import
echo ====================================================
echo.

REM Check if XAMPP MySQL is available
if not exist "C:\xampp\mysql\bin\mysql.exe" (
    echo ERROR: MySQL not found at C:\xampp\mysql\bin\mysql.exe
    echo Please ensure XAMPP is installed and MySQL is available.
    pause
    exit /b 1
)

REM Import the refactored data
echo Importing refactored schema and data into moh_db...
"C:\xampp\mysql\bin\mysql.exe" -u root moh_db < "C:\Users\ASUS\Desktop\MOH_Connect_Backend-main\DB\00_refactored_data.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================================
    echo SUCCESS! Database imported successfully
    echo ====================================================
    echo.
    echo The following data has been populated:
    echo - SESSION_TYPE (5 records)
    echo - SESSION (5 records)
    echo - MIDWIFE (3 records)
    echo - MOTHER (5 records)
    echo - BABY (5 records)
    echo - And all related reference data
    echo.
    echo You can now start the application!
    echo.
) else (
    echo.
    echo ====================================================
    echo ERROR! Database import failed
    echo ====================================================
    echo.
    echo Please check the error messages above and ensure:
    echo 1. MySQL is running (Start XAMPP Control Panel)
    echo 2. Database 'moh_db' exists
    echo 3. You have write permissions
    echo.
)

pause
