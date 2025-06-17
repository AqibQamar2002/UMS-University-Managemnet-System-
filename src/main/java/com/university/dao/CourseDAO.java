package com.university.dao;

import com.university.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CourseDAO {
    public int getCount() {
        String query = "SELECT COUNT(*) FROM courses";
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

    // Stub for enrolled courses (returns empty list for now)
    public List<Map<String, Object>> getEnrolledCourses(int userId) {
        return new ArrayList<>();
    }

    public boolean insertCourseWithLowestAvailableId(String courseName, String courseCode, int credits) {
        String findIdSql = "SELECT course_id FROM courses ORDER BY course_id";
        String insertSql = "INSERT INTO courses (course_id, course_name, course_code, credits) VALUES (?, ?, ?, ?)";

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
                ps.setString(2, courseName);
                ps.setString(3, courseCode);
                ps.setInt(4, credits);
                ps.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 