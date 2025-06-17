package com.university.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.university.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/enrollment-requests/*")
public class AdminEnrollmentRequestServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // List all pending requests
        List<JsonObject> requests = new ArrayList<>();
        String sql = "SELECT er.id, er.student_id, CONCAT(s.first_name, ' ', s.last_name) AS student_name, er.course_id, c.course_name, er.request_date, er.status " +
        "FROM enrollment_requests er " +
        "JOIN students s ON er.student_id = s.student_id " +
        "JOIN courses c ON er.course_id = c.course_id " ;
        // "WHERE er.status = 'PENDING'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("requestId", rs.getLong("id"));
                obj.addProperty("studentId", rs.getInt("student_id"));
                obj.addProperty("studentName", rs.getString("student_name"));
                obj.addProperty("courseId", rs.getInt("course_id"));
                obj.addProperty("courseName", rs.getString("course_name"));
                obj.addProperty("requestDate", rs.getTimestamp("request_date").toString());
                obj.addProperty("status", rs.getString("status"));
                requests.add(obj);
            }
        } catch (SQLException e) {
            response.sendError(500, "Database error: " + e.getMessage());
            return;
        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(requests));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        JsonObject req = gson.fromJson(request.getReader(), JsonObject.class);
        long requestId = req.get("requestId").getAsLong();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        int adminId = (int) session.getAttribute("userId");

        if ("/approve".equals(path)) {
            processRequest(requestId, "APPROVED", adminId, response);
        } else if ("/deny".equals(path)) {
            processRequest(requestId, "DENIED", adminId, response);
        } else {
            response.sendError(400, "Invalid action");
        }
    }

    private void processRequest(long requestId, String status, int adminId, HttpServletResponse response) throws IOException {
        String updateSql = "UPDATE enrollment_requests SET status = ?, processed_date = NOW(), processed_by = ? WHERE id = ?";
        String selectSql = "SELECT student_id, course_id FROM enrollment_requests WHERE id = ?";
        String enrollSql = "INSERT INTO enrolled_courses (student_id, course_id, status) VALUES (?, ?, 'ACTIVE')";
       // String deleteSql = "DELETE FROM enrollment_requests WHERE id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);

            // If approving, get student and course
            int studentId = 0, courseId = 0;
            if ("APPROVED".equals(status)) {
                try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                    selectStmt.setLong(1, requestId);
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (rs.next()) {
                            studentId = rs.getInt("student_id");
                            courseId = rs.getInt("course_id");
                        }
                    }
                }
            }

            // Update request status
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, status);
                updateStmt.setInt(2, adminId);
                updateStmt.setLong(3, requestId);
                updateStmt.executeUpdate();
            }

            // If approved, enroll student
            if ("APPROVED".equals(status)) {
                try (PreparedStatement enrollStmt = conn.prepareStatement(enrollSql)) {
                    enrollStmt.setInt(1, studentId);
                    enrollStmt.setInt(2, courseId);
                    enrollStmt.executeUpdate();
                }
            }

            // // Delete the request from enrollment_requests
            // try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            //     deleteStmt.setLong(1, requestId);
            //     deleteStmt.executeUpdate();
            // }

            conn.commit();
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":true}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(500, "Database error: " + e.getMessage());
        }
    }
} 