package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String HOST        = "localhost";
    private static final int    PORT        = 3306;
    private static final String DB_NAME     = "bloodbank";
    private static final String USER        = "root";
    private static final String PASSWORD    = "Kani@2904"; // <-- CHANGE PASSWORD HERE
    private static final String URL         = "jdbc:mysql://" + HOST + ":" + PORT + "/";
    private static final String URL_WITH_DB = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_WITH_DB, USER, PASSWORD);
    }

    public static void createDatabaseAndTables() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

            // ---------------------------------------------------------------- donor
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS donor (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    phone VARCHAR(15) NOT NULL UNIQUE,
                    email VARCHAR(100),
                    age INT NOT NULL CHECK (age BETWEEN 18 AND 65),
                    blood_group VARCHAR(5) NOT NULL,
                    address TEXT,
                    last_donation_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // ---------------------------------------------------------------- hospital  (must exist BEFORE patient FK)
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS hospital (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    phone VARCHAR(15) NOT NULL UNIQUE,
                    email VARCHAR(100),
                    address TEXT,
                    registration_number VARCHAR(50) UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // ---------------------------------------------------------------- patient
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS patient (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    phone VARCHAR(15) NOT NULL UNIQUE,
                    age INT NOT NULL CHECK (age > 0),
                    blood_group VARCHAR(5) NOT NULL,
                    address TEXT,
                    disease VARCHAR(200),
                    hospital_id INT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (hospital_id) REFERENCES hospital(id) ON DELETE SET NULL
                )
            """);

            // ---------------------------------------------------------------- blood_stock
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS blood_stock (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    blood_group VARCHAR(5) NOT NULL UNIQUE,
                    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0)
                )
            """);

            // ---------------------------------------------------------------- donation
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS donation (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    donor_id INT NOT NULL,
                    blood_group VARCHAR(5) NOT NULL,
                    units INT NOT NULL CHECK (units > 0),
                    donation_date DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (donor_id) REFERENCES donor(id) ON DELETE CASCADE
                )
            """);

            // ---------------------------------------------------------------- blood_request
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS blood_request (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    patient_id INT NOT NULL,
                    hospital_id INT NOT NULL,
                    blood_group VARCHAR(5) NOT NULL,
                    units INT NOT NULL CHECK (units > 0),
                    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
                    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
                    FOREIGN KEY (hospital_id) REFERENCES hospital(id) ON DELETE CASCADE
                )
            """);

            // Seed blood groups
            String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            for (String g : groups) {
                stmt.executeUpdate("INSERT IGNORE INTO blood_stock (blood_group, quantity) VALUES ('" + g + "', 0)");
            }

            System.out.println("[DB] Base tables initialised.");

            // ================================================================
            // SAFE MIGRATIONS  –  never drops data, only adds missing pieces
            // ================================================================
            runMigrations(conn);

            System.out.println("[DB] Database and tables initialised successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] Failed to initialise database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------------- migrations
    private static void runMigrations(Connection conn) throws SQLException {
        // 1. Add hospital_id to patient if missing
        if (!columnExists(conn, "patient", "hospital_id")) {
            System.out.println("[DB-MIGRATE] Adding patient.hospital_id …");
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(
                    "ALTER TABLE patient " +
                    "ADD COLUMN hospital_id INT NULL AFTER disease, " +
                    "ADD CONSTRAINT fk_patient_hospital " +
                    "  FOREIGN KEY (hospital_id) REFERENCES hospital(id) ON DELETE SET NULL"
                );
            }
            System.out.println("[DB-MIGRATE] patient.hospital_id added.");
        }

        // 2. Auto-generate registration_number codes for hospitals that have none
        //    Pattern: HOS-001, HOS-002, …
        autoGenerateHospitalCodes(conn);
    }

    /** Check whether a column exists in a table using INFORMATION_SCHEMA. */
    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DB_NAME);
            ps.setString(2, table);
            ps.setString(3, column);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /** For hospitals with a NULL registration_number, assign HOS-NNN codes. */
    private static void autoGenerateHospitalCodes(Connection conn) throws SQLException {
        // Find max existing numeric suffix to continue from there
        int maxSuffix = 0;
        String findMax = "SELECT registration_number FROM hospital WHERE registration_number LIKE 'HOS-%'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(findMax)) {
            while (rs.next()) {
                String code = rs.getString(1);
                try {
                    int n = Integer.parseInt(code.substring(4)); // after "HOS-"
                    if (n > maxSuffix) maxSuffix = n;
                } catch (Exception ignored) {}
            }
        }

        // Assign codes to hospitals that have none
        String findNull = "SELECT id FROM hospital WHERE registration_number IS NULL OR registration_number = '' ORDER BY id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(findNull)) {
            while (rs.next()) {
                maxSuffix++;
                String code = String.format("HOS-%03d", maxSuffix);
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "UPDATE hospital SET registration_number = ? WHERE id = ?")) {
                    ps.setString(1, code);
                    ps.setInt(2, rs.getInt("id"));
                    ps.executeUpdate();
                    System.out.println("[DB-MIGRATE] Assigned code " + code + " to hospital id=" + rs.getInt("id"));
                }
            }
        }
    }
}
