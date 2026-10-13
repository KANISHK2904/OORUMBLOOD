-- Blood Bank Management System - Database Schema
-- Run this on a clean MySQL install to set up the database manually.
-- The application also auto-creates this on startup.

CREATE DATABASE IF NOT EXISTS bloodbank;
USE bloodbank;

-- ============================================================
-- Table: donor
-- ============================================================
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
);

-- ============================================================
-- Table: patient
-- ============================================================
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
);

-- ============================================================
-- Table: hospital
-- ============================================================
CREATE TABLE IF NOT EXISTS hospital (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100),
    address TEXT,
    registration_number VARCHAR(50) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Table: blood_stock
-- ============================================================
CREATE TABLE IF NOT EXISTS blood_stock (
    id INT AUTO_INCREMENT PRIMARY KEY,
    blood_group VARCHAR(5) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0)
);

-- Seed: 8 blood groups with quantity 0
INSERT IGNORE INTO blood_stock (blood_group, quantity) VALUES
    ('A+',  0),
    ('A-',  0),
    ('B+',  0),
    ('B-',  0),
    ('AB+', 0),
    ('AB-', 0),
    ('O+',  0),
    ('O-',  0);

-- ============================================================
-- Table: donation  (FK: donor_id -> donor.id ON DELETE CASCADE)
-- ============================================================
CREATE TABLE IF NOT EXISTS donation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    donor_id INT NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    units INT NOT NULL CHECK (units > 0),
    donation_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES donor(id) ON DELETE CASCADE
);

-- ============================================================
-- Table: blood_request  (FK: patient_id, hospital_id ON DELETE CASCADE)
-- ============================================================
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
);
