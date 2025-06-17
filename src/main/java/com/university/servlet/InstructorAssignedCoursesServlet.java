package com.university.servlet;

import com.google.gson.Gson;
import com.university.util.DBUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/instructor/assigned-courses")
public class InstructorAssignedCoursesServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Not logged in\"}");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        Integer instructorId = getInstructorIdForUser(userId);
        if (instructorId == null) {
            response.getWriter().write("[]");
            return;
        }

        List<Map<String, Object>> courses = new ArrayList<>();
        String sql = "SELECT c.course_id, c.course_name, c.course_code, c.credits, ac.status " +
                     "FROM assigned_courses ac " +
                     "JOIN courses c ON ac.course_id = c.course_id " +
                     "WHERE ac.instructor_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> course = new HashMap<>();
                    course.put("courseId", rs.getInt("course_id"));
                    course.put("courseName", rs.getString("course_name"));
                    course.put("courseCode", rs.getString("course_code"));
                    course.put("credits", rs.getInt("credits"));
                    course.put("status", rs.getString("status"));
                    courses.add(course);
                }
            }
        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"message\":\"Database error: " + e.getMessage() + "\"}");
            return;
        }
        response.getWriter().write(gson.toJson(courses));
    }

    private Integer getInstructorIdForUser(int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT instructor_id FROM instructors WHERE email = (SELECT email FROM users WHERE id = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("instructor_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
} 