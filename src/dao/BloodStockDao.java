package dao;

import db.DatabaseConnection;
import model.BloodStock;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BloodStockDao {

    public List<BloodStock> findAll() throws Exception {
        List<BloodStock> list = new ArrayList<>();
        String sql = "SELECT * FROM blood_stock ORDER BY blood_group";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new BloodStock(rs.getInt("id"), rs.getString("blood_group"), rs.getInt("quantity")));
            }
        }
        return list;
    }

    public BloodStock findByGroup(String bloodGroup) throws Exception {
        String sql = "SELECT * FROM blood_stock WHERE blood_group = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bloodGroup);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new BloodStock(rs.getInt("id"), rs.getString("blood_group"), rs.getInt("quantity"));
        }
        return null;
    }

    public void updateQuantity(Connection conn, String bloodGroup, int delta) throws Exception {
        String sql = "UPDATE blood_stock SET quantity = quantity + ? WHERE blood_group = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, bloodGroup);
            ps.executeUpdate();
        }
    }
}
