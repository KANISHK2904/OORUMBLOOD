#!/bin/bash
# ================================================================
# Oorum Blood – Blood Bank Management System
# Build & Run Script (Linux / macOS)
# ================================================================
# Prerequisites:
#   1. JDK 17+ on PATH
#   2. MySQL Server running (user=root, password=root by default)
#   3. MySQL Connector/J jar placed in lib/
# ================================================================

set -e

echo "[Oorum Blood] Checking JDK..."
java -version 2>/dev/null || { echo "ERROR: Java not found. Install JDK 17+."; exit 1; }

# Resolve connector jar
CONNECTOR=$(ls lib/*.jar 2>/dev/null | head -1)
if [ -z "$CONNECTOR" ]; then
  echo "ERROR: No jar found in lib/"
  echo "Download mysql-connector-j-x.x.x.jar and place it in lib/"
  exit 1
fi
echo "[Oorum Blood] Using connector: $CONNECTOR"

# Compile
echo "[Oorum Blood] Compiling Java sources..."
mkdir -p out

javac -cp "$CONNECTOR" -d out -sourcepath src \
  src/Main.java \
  src/db/DatabaseConnection.java \
  src/model/Person.java \
  src/model/Donor.java \
  src/model/Patient.java \
  src/model/Hospital.java \
  src/model/BloodStock.java \
  src/model/Donation.java \
  src/model/BloodRequest.java \
  src/dao/CrudOperations.java \
  src/dao/DonorDao.java \
  src/dao/PatientDao.java \
  src/dao/HospitalDao.java \
  src/dao/BloodStockDao.java \
  src/dao/DonationDao.java \
  src/dao/BloodRequestDao.java \
  src/util/JsonUtil.java \
  src/server/BaseHandler.java \
  src/server/StaticFileHandler.java \
  src/server/DashboardHandler.java \
  src/server/DonorHandler.java \
  src/server/PatientHandler.java \
  src/server/HospitalHandler.java \
  src/server/BloodStockHandler.java \
  src/server/DonationHandler.java \
  src/server/BloodRequestHandler.java

echo "[Oorum Blood] Compilation successful."
echo "[Oorum Blood] Starting server → http://localhost:8080"
echo "[Oorum Blood] Press Ctrl+C to stop."
echo ""

java -cp "out:$CONNECTOR" Main
