@echo off
REM ================================================================
REM Oorum Blood – Blood Bank Management System
REM Build & Run Script (Windows)
REM ================================================================
REM Prerequisites:
REM   1. JDK 17+ on PATH  (java, javac)
REM   2. MySQL Server running (user=root, password=root by default)
REM   3. MySQL Connector/J jar placed in lib\  (any version 8.x/9.x)
REM ================================================================

setlocal EnableDelayedExpansion

echo [Oorum Blood] Checking JDK...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Install JDK 17+ and add it to PATH.
    pause & exit /b 1
)

REM ---- Resolve lib jar ----
set "CONNECTOR="
for %%f in (lib\*.jar) do set "CONNECTOR=%%f"
if not defined CONNECTOR (
    echo ERROR: No jar found in lib\
    echo Please download mysql-connector-j-x.x.x.jar and place it in lib\
    pause & exit /b 1
)
echo [Oorum Blood] Using connector: %CONNECTOR%

REM ---- Compile ----
echo [Oorum Blood] Compiling Java sources...
if not exist out mkdir out

javac -cp "%CONNECTOR%" -d out -sourcepath src ^
    src\Main.java ^
    src\db\DatabaseConnection.java ^
    src\model\Person.java ^
    src\model\Donor.java ^
    src\model\Patient.java ^
    src\model\Hospital.java ^
    src\model\BloodStock.java ^
    src\model\Donation.java ^
    src\model\BloodRequest.java ^
    src\dao\CrudOperations.java ^
    src\dao\DonorDao.java ^
    src\dao\PatientDao.java ^
    src\dao\HospitalDao.java ^
    src\dao\BloodStockDao.java ^
    src\dao\DonationDao.java ^
    src\dao\BloodRequestDao.java ^
    src\util\JsonUtil.java ^
    src\server\BaseHandler.java ^
    src\server\StaticFileHandler.java ^
    src\server\DashboardHandler.java ^
    src\server\DonorHandler.java ^
    src\server\PatientHandler.java ^
    src\server\HospitalHandler.java ^
    src\server\BloodStockHandler.java ^
    src\server\DonationHandler.java ^
    src\server\BloodRequestHandler.java

if %errorlevel% neq 0 (
    echo ERROR: Compilation failed. See errors above.
    pause & exit /b 1
)
echo [Oorum Blood] Compilation successful.

REM ---- Run ----
echo [Oorum Blood] Starting server on http://localhost:8080 ...
echo [Oorum Blood] Press Ctrl+C to stop.
echo.
java -cp "out;%CONNECTOR%" Main

endlocal
