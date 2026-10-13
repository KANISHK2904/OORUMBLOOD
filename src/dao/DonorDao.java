package dao;

import db.DatabaseConnection;
import model.Donor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorDao implements CrudOperations<Donor> {

    @Override
    public void create(Donor donor) throws Exception {
        String sql = "INSERT INTO donor (name, phone, email, age, blood_group, address, last_donation_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donor.getName());
            ps.setString(2, donor.getPhone());
            ps.setString(3, donor.getEmail());
            ps.setInt(4, donor.getAge());
            ps.setString(5, donor.getBloodGroup());
            ps.setString(6, donor.getAddress());
            if (donor.getLastDonationDate() != null && !donor.getLastDonationDate().isEmpty()) {
                ps.setDate(7, Date.valueOf(donor.getLastDonationDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.executeUpdate();
        }
    }

    @Override
    public Donor findById(int id) throws Exception {
        String sql = "SELECT * FROM donor WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<Donor> findAll() throws Exception {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donor ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public void update(Donor donor) throws Exception {
        String sql = "UPDATE donor SET name=?, phone=?, email=?, age=?, blood_group=?, address=?, last_donation_date=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donor.getName());
            ps.setString(2, donor.getPhone());
            ps.setString(3, donor.getEmail());
            ps.setInt(4, donor.getAge());
            ps.setString(5, donor.getBloodGroup());
            ps.setString(6, donor.getAddress());
            if (donor.getLastDonationDate() != null && !donor.getLastDonationDate().isEmpty()) {
                ps.setDate(7, Date.valueOf(donor.getLastDonationDate()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setInt(8, donor.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        String sql = "DELETE FROM donor WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Donor> search(String query) throws Exception {
        List<Donor> list = new ArrayList<>();
        String sql = "SELECT * FROM donor WHERE name LIKE ? OR blood_group = ? ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Donor mapRow(ResultSet rs) throws SQLException {
        Date lastDate = rs.getDate("last_donation_date");
        return new Donor(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getInt("age"),
            rs.getString("blood_group"),
            rs.getString("address"),
            lastDate != null ? lastDate.toString() : ""
        );
    }
}
