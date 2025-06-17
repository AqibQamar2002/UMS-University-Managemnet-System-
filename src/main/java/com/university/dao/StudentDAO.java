package com.university.dao;

import com.university.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentDAO {
    public int getCount() {
        String query = "SELECT COUNT(*) FROM students";
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

    public boolean insertStudentWithLowestAvailableId(String firstName, String lastName, String dateOfBirth, String email) {
        String findIdSql = "SELECT student_id FROM students ORDER BY student_id";
        String insertSql = "INSERT INTO students (student_id, first_name, last_name, date_of_birth, email) VALUES (?, ?, ?, ?, ?)";

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
                ps.setString(4, dateOfBirth);
                ps.setString(5, email);
                ps.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 