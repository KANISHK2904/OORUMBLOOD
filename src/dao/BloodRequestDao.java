package dao;

import db.DatabaseConnection;
import model.BloodRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BloodRequestDao {
    private final BloodStockDao stockDao = new BloodStockDao();

    public void create(BloodRequest req) throws Exception {
        String sql = "INSERT INTO blood_request (patient_id, hospital_id, blood_group, units, status) VALUES (?, ?, ?, ?, 'Pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, req.getPatientId());
            ps.setInt(2, req.getHospitalId());
            ps.setString(3, req.getBloodGroup());
            ps.setInt(4, req.getUnits());
            ps.executeUpdate();
        }
    }

    public List<BloodRequest> findAll() throws Exception {
        List<BloodRequest> list = new ArrayList<>();
        // JOIN with patient and hospital
        String sql = "SELECT br.id, br.patient_id, p.name as patient_name, br.hospital_id, h.name as hospital_name, " +
                     "br.blood_group, br.units, br.status, br.request_date " +
                     "FROM blood_request br " +
                     "JOIN patient p ON br.patient_id = p.id " +
                     "JOIN hospital h ON br.hospital_id = h.id " +
                     "ORDER BY br.request_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<BloodRequest> findRecent(int limit) throws Exception {
        List<BloodRequest> list = new ArrayList<>();
        String sql = "SELECT br.id, br.patient_id, p.name as patient_name, br.hospital_id, h.name as hospital_name, " +
                     "br.blood_group, br.units, br.status, br.request_date " +
                     "FROM blood_request br " +
                     "JOIN patient p ON br.patient_id = p.id " +
                     "JOIN hospital h ON br.hospital_id = h.id " +
                     "ORDER BY br.id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Fulfill a request in a transaction: check stock, deduct, update status. Returns error message or null on success. */
    public String fulfill(int requestId) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            // Get request
            String getReq = "SELECT * FROM blood_request WHERE id = ? FOR UPDATE";
            BloodRequest req = null;
            try (PreparedStatement ps = conn.prepareStatement(getReq)) {
                ps.setInt(1, requestId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    req = new BloodRequest();
                    req.setId(rs.getInt("id"));
                    req.setBloodGroup(rs.getString("blood_group"));
                    req.setUnits(rs.getInt("units"));
                    req.setStatus(rs.getString("status"));
                }
            }
            if (req == null) { conn.rollback(); return "Request not found."; }
            if (!"Pending".equals(req.getStatus())) { conn.rollback(); return "Request is already " + req.getStatus() + "."; }

            // Check stock
            String checkStock = "SELECT quantity FROM blood_stock WHERE blood_group = ? FOR UPDATE";
            int available = 0;
            try (PreparedStatement ps = conn.prepareStatement(checkStock)) {
                ps.setString(1, req.getBloodGroup());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) available = rs.getInt("quantity");
            }
            if (available < req.getUnits()) {
                conn.rollback();
                return "Insufficient stock. Available: " + available + " units of " + req.getBloodGroup() + ", Requested: " + req.getUnits() + " units.";
            }

            // Deduct stock
            stockDao.updateQuantity(conn, req.getBloodGroup(), -req.getUnits());

            // Update status
            String updateStatus = "UPDATE blood_request SET status = 'Fulfilled' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateStatus)) {
                ps.setInt(1, requestId);
                ps.executeUpdate();
            }
            conn.commit();
            return null; // success
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public void reject(int requestId) throws Exception {
        String sql = "UPDATE blood_request SET status = 'Rejected' WHERE id = ? AND status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    public void delete(int requestId) throws Exception {
        String sql = "DELETE FROM blood_request WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    private BloodRequest mapRow(ResultSet rs) throws SQLException {
        return new BloodRequest(
            rs.getInt("id"),
            rs.getInt("patient_id"),
            rs.getString("patient_name"),
            rs.getInt("hospital_id"),
            rs.getString("hospital_name"),
            rs.getString("blood_group"),
            rs.getInt("units"),
            rs.getString("status"),
            rs.getTimestamp("request_date").toString()
        );
    }
}
