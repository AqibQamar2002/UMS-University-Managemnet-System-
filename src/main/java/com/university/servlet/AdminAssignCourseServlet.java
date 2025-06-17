package com.university.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.university.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {
    "/admin/instructors",
    "/admin/courses",
    "/admin/assign-course",
    "/admin/assigned-courses"
})
public class AdminAssignCourseServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();
        response.setContentType("application/json");
        try (Connection conn = DBUtil.getConnection()) {
            if ("/admin/instructors".equals(path)) {
                List<Map<String, Object>> instructors = new ArrayList<>();
                String sql = "SELECT instructor_id, first_name, last_name FROM instructors";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> instructor = new HashMap<>();
                        instructor.put("instructorId", rs.getInt("instructor_id"));
                        instructor.put("firstName", rs.getString("first_name"));
                        instructor.put("lastName", rs.getString("last_name"));
                        instructors.add(instructor);
                    }
                }
                response.getWriter().write(gson.toJson(instructors));
            } else if ("/admin/courses".equals(path)) {
                List<Map<String, Object>> courses = new ArrayList<>();
                String sql = "SELECT course_id, course_name FROM courses";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> course = new HashMap<>();
                        course.put("courseId", rs.getInt("course_id"));
                        course.put("courseName", rs.getString("course_name"));
                        courses.add(course);
                    }
                }
                response.getWriter().write(gson.toJson(courses));
            } else if ("/admin/assigned-courses".equals(path)) {
                List<Map<String, Object>> assignments = new ArrayList<>();
                String sql = "SELECT ac.id, " +
                             "CONCAT(i.first_name, ' ', i.last_name) AS instructorName, " +
                             "c.course_name AS courseName, " +
                             "ac.assigned_date, ac.status " +
                             "FROM assigned_courses ac " +
                             "JOIN instructors i ON ac.instructor_id = i.instructor_id " +
                             "JOIN courses c ON ac.course_id = c.course_id";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> assignment = new HashMap<>();
                        assignment.put("id", rs.getLong("id"));
                        assignment.put("instructorName", rs.getString("instructorName"));
                        assignment.put("courseName", rs.getString("courseName"));
                        assignment.put("assignedDate", rs.getString("assigned_date"));
                        assignment.put("status", rs.getString("status"));
                        assignments.add(assignment);
                    }
                }
                response.getWriter().write(gson.toJson(assignments));
            }
        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"message\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();
        response.setContentType("application/json");
        if ("/admin/assign-course".equals(path)) {
            JsonObject req = gson.fromJson(request.getReader(), JsonObject.class);
            int instructorId = req.get("instructorId").getAsInt();
            int courseId = req.get("courseId").getAsInt();

            try (Connection conn = DBUtil.getConnection()) {
                // Check for duplicate assignment
                String checkSql = "SELECT COUNT(*) FROM assigned_courses WHERE instructor_id = ? AND course_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, instructorId);
                    checkStmt.setInt(2, courseId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        response.getWriter().write("{\"success\":false,\"message\":\"This course is already assigned to this instructor.\"}");
                        return;
                    }
                }
                // Insert assignment
                String sql = "INSERT INTO assigned_courses (instructor_id, course_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, instructorId);
                    stmt.setInt(2, courseId);
                    stmt.executeUpdate();
                }
                response.getWriter().write("{\"success\":true,\"message\":\"Course assigned successfully!\"}");
            } catch (SQLException e) {
                response.setStatus(500);
                response.getWriter().write("{\"success\":false,\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        }
    }
} 