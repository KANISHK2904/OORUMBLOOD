package dao;

import db.DatabaseConnection;
import model.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PatientDao — CRUD for patients, with LEFT JOIN on hospital for name/code.
 * Implements CrudOperations<Patient> (Abstraction via interface).
 */
public class PatientDao implements CrudOperations<Patient> {

    // ------------------------------------------------------------------ CREATE
    @Override
    public void create(Patient p) throws Exception {
        String sql = "INSERT INTO patient (name, phone, age, blood_group, address, disease, hospital_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getPhone());
            ps.setInt(3, p.getAge());
            ps.setString(4, p.getBloodGroup());
            ps.setString(5, p.getAddress());
            ps.setString(6, p.getDisease());
            setNullableHospital(ps, 7, p.getHospitalId());
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ FIND BY ID
    @Override
    public Patient findById(int id) throws Exception {
        String sql = "SELECT p.*, h.name AS h_name, h.registration_number AS h_code " +
                     "FROM patient p " +
                     "LEFT JOIN hospital h ON p.hospital_id = h.id " +
                     "WHERE p.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    // ------------------------------------------------------------------ FIND ALL
    @Override
    public List<Patient> findAll() throws Exception {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT p.*, h.name AS h_name, h.registration_number AS h_code " +
                     "FROM patient p " +
                     "LEFT JOIN hospital h ON p.hospital_id = h.id " +
                     "ORDER BY p.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ------------------------------------------------------------------ UPDATE
    @Override
    public void update(Patient p) throws Exception {
        String sql = "UPDATE patient SET name=?, phone=?, age=?, blood_group=?, " +
                     "address=?, disease=?, hospital_id=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getPhone());
            ps.setInt(3, p.getAge());
            ps.setString(4, p.getBloodGroup());
            ps.setString(5, p.getAddress());
            ps.setString(6, p.getDisease());
            setNullableHospital(ps, 7, p.getHospitalId());
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ DELETE
    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM patient WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ HELPERS
    /** Sets hospital_id parameter; uses NULL when id == 0. */
    private void setNullableHospital(PreparedStatement ps, int idx, int hospitalId) throws SQLException {
        if (hospitalId > 0) {
            ps.setInt(idx, hospitalId);
        } else {
            ps.setNull(idx, Types.INTEGER);
        }
    }

    /** Maps a ResultSet row that includes h_name and h_code from the LEFT JOIN. */
    private Patient mapRow(ResultSet rs) throws SQLException {
        int hid  = rs.getInt("hospital_id");
        if (rs.wasNull()) hid = 0;
        String hName = rs.getString("h_name");
        String hCode = rs.getString("h_code");
        return new Patient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getInt("age"),
            rs.getString("blood_group"),
            rs.getString("address"),
            rs.getString("disease"),
            hid,
            hName != null ? hName : "",
            hCode != null ? hCode : ""
        );
    }
}
