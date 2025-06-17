package com.university.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.university.util.DBUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/admin/users/*")
public class AdminUserManagementServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // List all users
        List<JsonObject> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name, email, role FROM users";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                JsonObject user = new JsonObject();
                user.addProperty("id", rs.getInt("id"));
                user.addProperty("firstName", rs.getString("first_name"));
                user.addProperty("lastName", rs.getString("last_name"));
                user.addProperty("email", rs.getString("email"));
                user.addProperty("role", rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            response.sendError(500, "Database error: " + e.getMessage());
            return;
        }
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(users));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        JsonObject req = gson.fromJson(request.getReader(), JsonObject.class);
        if (path == null || "/role".equals(path)) {
            // Set user role
            int userId = req.get("userId").getAsInt();
            String role = req.get("role").getAsString();
            String sql = "UPDATE users SET role = ? WHERE id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, role);
                stmt.setInt(2, userId);
                stmt.executeUpdate();

                // Add to students table if role is STUDENT
                if ("STUDENT".equals(role)) {
                    String checkSql = "SELECT COUNT(*) FROM students WHERE email = (SELECT email FROM users WHERE id = ?)";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, userId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            String insertSql = "INSERT INTO students (first_name, last_name, email) SELECT first_name, last_name, email FROM users WHERE id = ?";
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                insertStmt.setInt(1, userId);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                }
                // Add to instructors table if role is INSTRUCTOR
                if ("INSTRUCTOR".equals(role)) {
                    String checkSql = "SELECT COUNT(*) FROM instructors WHERE email = (SELECT email FROM users WHERE id = ?)";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, userId);
                        ResultSet rs = checkStmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            String insertSql = "INSERT INTO instructors (first_name, last_name, email) SELECT first_name, last_name, email FROM users WHERE id = ?";
                            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                                insertStmt.setInt(1, userId);
                                insertStmt.executeUpdate();
                            }
                        }
                    }
                }

                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true}");
            } catch (SQLException e) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else if ("/add-student".equals(path)) {
            // Add user as student
            int userId = req.get("userId").getAsInt();
            String userSql = "SELECT first_name, last_name, email FROM users WHERE id = ?";
            String checkSql = "SELECT * FROM students WHERE email = ?";
            String insertSql = "INSERT INTO students (first_name, last_name, email) VALUES (?, ?, ?)";
            try (Connection conn = DBUtil.getConnection()) {
                String firstName = null, lastName = null, email = null;
                try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                    userStmt.setInt(1, userId);
                    ResultSet rs = userStmt.executeQuery();
                    if (rs.next()) {
                        firstName = rs.getString("first_name");
                        lastName = rs.getString("last_name");
                        email = rs.getString("email");
                    } else {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"message\":\"User not found.\"}");
                        return;
                    }
                }
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, email);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"message\":\"User is already a student.\"}");
                        return;
                    }
                }
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, firstName);
                    insertStmt.setString(2, lastName);
                    insertStmt.setString(3, email);
                    insertStmt.executeUpdate();
                }
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"message\":\"User added as student.\"}");
            } catch (SQLException e) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else if ("/add-instructor".equals(path)) {
            // Add user as instructor
            int userId = req.get("userId").getAsInt();
            String userSql = "SELECT first_name, last_name, email FROM users WHERE id = ?";
            String checkSql = "SELECT * FROM instructors WHERE email = ?";
            String insertSql = "INSERT INTO instructors (first_name, last_name, email) VALUES (?, ?, ?)";
            try (Connection conn = DBUtil.getConnection()) {
                String firstName = null, lastName = null, email = null;
                try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                    userStmt.setInt(1, userId);
                    ResultSet rs = userStmt.executeQuery();
                    if (rs.next()) {
                        firstName = rs.getString("first_name");
                        lastName = rs.getString("last_name");
                        email = rs.getString("email");
                    } else {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"message\":\"User not found.\"}");
                        return;
                    }
                }
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, email);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\":false,\"message\":\"User is already an instructor.\"}");
                        return;
                    }
                }
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, firstName);
                    insertStmt.setString(2, lastName);
                    insertStmt.setString(3, email);
                    insertStmt.executeUpdate();
                }
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":true,\"message\":\"User added as instructor.\"}");
            } catch (SQLException e) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            response.sendError(400, "Invalid action");
        }
    }
} 