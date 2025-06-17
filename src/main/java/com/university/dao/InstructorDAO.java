package com.university.dao;

import com.university.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InstructorDAO {
    public int getCount() {
        String query = "SELECT COUNT(*) FROM instructors";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean insertInstructorWithLowestAvailableId(String firstName, String lastName, String email, String department) {
        String findIdSql = "SELECT instructor_id FROM instructors ORDER BY instructor_id";
        String insertSql = "INSERT INTO instructors (instructor_id, first_name, last_name, email, department) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(findIdSql)) {

            int nextId = 1;
            while (rs.next()) {
                int currentId = rs.getInt(1);
                if (currentId == nextId) {
                    nextId++;
                } else {
                    break; // Found a gap
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, nextId);
                ps.setString(2, firstName);
                ps.setString(3, lastName);
                ps.setString(4, email);
                ps.setString(5, department);
                ps.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 