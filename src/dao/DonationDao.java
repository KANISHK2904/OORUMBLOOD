package dao;

import db.DatabaseConnection;
import model.Donation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationDao {
    private final BloodStockDao stockDao = new BloodStockDao();

    public void recordDonation(Donation donation) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);
            // Insert donation
            String sql = "INSERT INTO donation (donor_id, blood_group, units, donation_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, donation.getDonorId());
                ps.setString(2, donation.getBloodGroup());
                ps.setInt(3, donation.getUnits());
                ps.setDate(4, Date.valueOf(donation.getDonationDate()));
                ps.executeUpdate();
            }
            // Update last donation date on donor
            String updateDonor = "UPDATE donor SET last_donation_date = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateDonor)) {
                ps.setDate(1, Date.valueOf(donation.getDonationDate()));
                ps.setInt(2, donation.getDonorId());
                ps.executeUpdate();
            }
            // Increase stock
            stockDao.updateQuantity(conn, donation.getBloodGroup(), donation.getUnits());
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public List<Donation> findAll() throws Exception {
        List<Donation> list = new ArrayList<>();
        // JOIN with donor to get donor name
        String sql = "SELECT d.id, d.donor_id, dn.name as donor_name, d.blood_group, d.units, d.donation_date " +
                     "FROM donation d JOIN donor dn ON d.donor_id = dn.id ORDER BY d.donation_date DESC, d.id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Donation(
                    rs.getInt("id"),
                    rs.getInt("donor_id"),
                    rs.getString("donor_name"),
                    rs.getString("blood_group"),
                    rs.getInt("units"),
                    rs.getDate("donation_date").toString()
                ));
            }
        }
        return list;
    }

    public List<Donation> findRecent(int limit) throws Exception {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT d.id, d.donor_id, dn.name as donor_name, d.blood_group, d.units, d.donation_date " +
                     "FROM donation d JOIN donor dn ON d.donor_id = dn.id ORDER BY d.id DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Donation(
                    rs.getInt("id"),
                    rs.getInt("donor_id"),
                    rs.getString("donor_name"),
                    rs.getString("blood_group"),
                    rs.getInt("units"),
                    rs.getDate("donation_date").toString()
                ));
            }
        }
        return list;
    }
}
