package dao;

import db.DatabaseConnection;
import model.Hospital;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HospitalDao — CRUD for hospitals plus live search endpoint.
 * Implements CrudOperations<Hospital> (Abstraction via interface).
 */
public class HospitalDao implements CrudOperations<Hospital> {

    // ------------------------------------------------------------------ CREATE
    @Override
    public void create(Hospital h) throws Exception {
        String sql = "INSERT INTO hospital (name, phone, email, address, registration_number) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getName());
            ps.setString(2, h.getPhone());
            ps.setString(3, h.getEmail());
            ps.setString(4, h.getAddress());
            setNullableStr(ps, 5, h.getRegistrationNumber());
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ FIND BY ID
    @Override
    public Hospital findById(int id) throws Exception {
        String sql = "SELECT * FROM hospital WHERE id = ?";
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
    public List<Hospital> findAll() throws Exception {
        List<Hospital> list = new ArrayList<>();
        String sql = "SELECT * FROM hospital ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ------------------------------------------------------------------ SEARCH
    /**
     * Live search: partial, case-insensitive match on hospital name OR
     * registration_number (the "hospital code"), ordered by name, max 8 results.
     * Used by GET /api/hospitals/search?q=
     */
    public List<Hospital> search(String query) throws Exception {
        List<Hospital> list = new ArrayList<>();
        String like = "%" + query + "%";
        String sql = "SELECT * FROM hospital " +
                     "WHERE name LIKE ? OR registration_number LIKE ? " +
                     "ORDER BY name LIMIT 8";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ------------------------------------------------------------------ UPDATE
    @Override
    public void update(Hospital h) throws Exception {
        String sql = "UPDATE hospital SET name=?, phone=?, email=?, address=?, " +
                     "registration_number=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getName());
            ps.setString(2, h.getPhone());
            ps.setString(3, h.getEmail());
            ps.setString(4, h.getAddress());
            setNullableStr(ps, 5, h.getRegistrationNumber());
            ps.setInt(6, h.getId());
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ DELETE
    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM hospital WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------ HELPERS
    private void setNullableStr(PreparedStatement ps, int idx, String val) throws SQLException {
        if (val != null && !val.isBlank()) {
            ps.setString(idx, val.trim());
        } else {
            ps.setNull(idx, Types.VARCHAR);
        }
    }

    private Hospital mapRow(ResultSet rs) throws SQLException {
        return new Hospital(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            rs.getString("registration_number")
        );
    }
}
