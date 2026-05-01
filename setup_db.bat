@echo off
REM Setup MOH Connect Database
echo.
echo ========================================
echo MOH Connect - Database Setup
echo ========================================
echo.

REM Check if MySQL is available
"C:\xampp\mysql\bin\mysql.exe" --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: MySQL not found at C:\xampp\mysql\bin\mysql.exe
    echo Please ensure XAMPP is installed and MySQL is running.
    pause
    exit /b 1
)

echo [1/3] Creating database and tables...
"C:\xampp\mysql\bin\mysql.exe" -u root < ".\DB\01_schema.sql"
if errorlevel 1 (
    echo ERROR: Failed to create schema
    pause
    exit /b 1
)

echo [2/3] Inserting PHM Area data...
"C:\xampp\mysql\bin\mysql.exe" -u root moh_db -e "INSERT INTO PHM_AREA (area_name, area_code) VALUES ('Colombo 01', 'CMB01'), ('Colombo 02', 'CMB02'), ('Gampaha', 'GAM01'), ('Kalutara', 'KAL01');"
if errorlevel 1 (
    echo WARNING: PHM data insert had issues (may already exist)
)

echo [3/3] Verifying database...
"C:\xampp\mysql\bin\mysql.exe" -u root moh_db -e "SELECT COUNT(*) as 'Total Tables' FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='moh_db';"

echo.
echo ========================================
echo Database setup completed!
echo ========================================
pause
