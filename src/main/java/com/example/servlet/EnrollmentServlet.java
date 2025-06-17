package com.example.servlet;

import com.example.util.DatabaseUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/enrollment/*")
public class EnrollmentServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        Integer studentId = (Integer) session.getAttribute("studentId");

        if (studentId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all enrolled courses
                List<JsonObject> courses = getEnrolledCourses(conn, studentId);
                sendJsonResponse(response, courses);
            } else if (pathInfo.equals("/pending")) {
                // Get pending enrollments
                List<JsonObject> pendingCourses = getPendingEnrollments(conn, studentId);
                sendJsonResponse(response, pendingCourses);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        Integer studentId = (Integer) session.getAttribute("studentId");

        if (studentId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Enroll in a course
                JsonObject requestData = gson.fromJson(request.getReader(), JsonObject.class);
                int courseId = requestData.get("courseId").getAsInt();
                enrollInCourse(conn, studentId, courseId);
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else if (pathInfo.equals("/unenroll")) {
                // Unenroll from a course
                JsonObject requestData = gson.fromJson(request.getReader(), JsonObject.class);
                int courseId = requestData.get("courseId").getAsInt();
                unenrollFromCourse(conn, studentId, courseId);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    private List<JsonObject> getEnrolledCourses(Connection conn, int studentId) throws SQLException {
        List<JsonObject> courses = new ArrayList<>();
        String sql = "SELECT c.*, ec.status, ec.grade FROM courses c " +
                    "JOIN enrolled_courses ec ON c.id = ec.course_id " +
                    "WHERE ec.student_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JsonObject course = new JsonObject();
                    course.addProperty("id", rs.getInt("id"));
                    course.addProperty("name", rs.getString("name"));
                    course.addProperty("description", rs.getString("description"));
                    course.addProperty("status", rs.getString("status"));
                    course.addProperty("grade", rs.getString("grade"));
                    courses.add(course);
                }
            }
        }
        return courses;
    }

    private List<JsonObject> getPendingEnrollments(Connection conn, int studentId) throws SQLException {
        List<JsonObject> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM courses c " +
                    "JOIN enrolled_courses ec ON c.id = ec.course_id " +
                    "WHERE ec.student_id = ? AND ec.status = 'pending'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JsonObject course = new JsonObject();
                    course.addProperty("id", rs.getInt("id"));
                    course.addProperty("name", rs.getString("name"));
                    course.addProperty("description", rs.getString("description"));
                    courses.add(course);
                }
            }
        }
        return courses;
    }

    private void enrollInCourse(Connection conn, int studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO enrolled_courses (student_id, course_id, status) VALUES (?, ?, 'pending')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.executeUpdate();
        }
    }

    private void unenrollFromCourse(Connection conn, int studentId, int courseId) throws SQLException {
        String sql = "DELETE FROM enrolled_courses WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            stmt.executeUpdate();
        }
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }
}