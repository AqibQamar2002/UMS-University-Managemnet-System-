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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/enrolled-courses")
public class EnrollmentServlet extends HttpServlet {
    private Gson gson = new Gson();

    private Integer getStudentIdForUser(int userId) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT student_id FROM students WHERE email = (SELECT email FROM users WHERE id = ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("student_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        Integer studentId = getStudentIdForUser(userId);
        if (studentId == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"You are not registered as a student.\"}");
            return;
        }
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to get database connection");
            }

            if (action == null || "enrolled".equals(action)) {
                List<JsonObject> courses = getEnrolledCourses(conn, studentId);
                sendJsonResponse(response, courses);
            } else if ("pending".equals(action)) {
                List<JsonObject> pendingCourses = getPendingEnrollments(conn, studentId);
                sendJsonResponse(response, pendingCourses);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Database error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        Integer userId = null;

        // If JSON, parse action and userId from body
        JsonObject reqBody = null;
        if (request.getContentType() != null && request.getContentType().contains("application/json")) {
            reqBody = new Gson().fromJson(request.getReader(), JsonObject.class);
            if (reqBody.has("action")) {
                action = reqBody.get("action").getAsString();
            }
            if (reqBody.has("userId")) {
                userId = reqBody.get("userId").getAsInt();
            }
            if (reqBody.has("courseId")) {
                request.setAttribute("courseId", reqBody.get("courseId").getAsInt());
            }
        }
        if (userId == null) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                userId = (int) session.getAttribute("userId");
            }
        }
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        Integer studentId = getStudentIdForUser(userId);
        if (studentId == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"You are not registered as a student.\"}");
            return;
        }

        request.setAttribute("userId", userId);
        request.setAttribute("studentId", studentId);

        if ("enroll".equals(action)) {
            enrollInCourse(request, response);
        } else if ("unenroll".equals(action)) {
            unenrollFromCourse(request, response);
        } 
        //else if ("cancel".equals(action)) {
        //cancelEnrollment(request, response);
        // } 
        else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    // private void getEnrolledCourses(HttpServletRequest request, HttpServletResponse response) 
    //         throws IOException {
    //     HttpSession session = request.getSession(false);
    //     if (session == null || session.getAttribute("userId") == null) {
    //         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
    //         return;
    //     }

    //     int studentId = (int) session.getAttribute("userId");
    //     List<JsonObject> enrolledCourses = new ArrayList<>();

    //     try (Connection conn = DBUtil.getConnection()) {
    //         enrolledCourses = getEnrolledCourses(conn, studentId);
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
    //         return;
    //     }

    //     response.setContentType("application/json");
    //     response.getWriter().write(gson.toJson(enrolledCourses));
    // }

    private List<JsonObject> getEnrolledCourses(Connection conn, Integer userId) throws SQLException {
        List<JsonObject> courses = new ArrayList<>();
        String sql = "SELECT c.*, ec.status, ec.grade FROM courses c " +
                    "JOIN enrolled_courses ec ON c.course_id = ec.course_id " +
                    "WHERE ec.student_id = ? AND ec.status = 'ACTIVE'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JsonObject course = new JsonObject();
                    course.addProperty("courseId", rs.getInt("course_id"));
                    course.addProperty("courseName", rs.getString("course_name"));
                    course.addProperty("courseCode", rs.getString("course_code"));
                    course.addProperty("credits", rs.getInt("credits"));
                    course.addProperty("status", rs.getString("status"));
                    course.addProperty("grade", rs.getString("grade"));
                    courses.add(course);
                }
            }
        }
        return courses;
    }

    // private boolean isEnrolledInCourseWithStatusDropped(Connection conn, Integer studentId, Integer courseId) throws SQLException {
    //     String sql = "SELECT COUNT(*) FROM enrolled_courses WHERE student_id = ? AND course_id = ? AND status = 'DROPPED'";
    //     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    //         stmt.setInt(1, studentId);
    //         stmt.setInt(2, courseId);
    //         try (ResultSet rs = stmt.executeQuery()) {
    //             if (rs.next()) {
    //                 return rs.getInt(1) > 0;
    //             }
    //         }
    //     }
    //     return false;
    // }


    private void enrollInCourse(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }
        int userId = (int) session.getAttribute("userId");

        Integer studentId = getStudentIdForUser(userId);
        if (studentId == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"You are not registered as a student.\"}");
            return;
        }

        int courseId;
        try {
            if (request.getAttribute("courseId") != null) {
                courseId = (int) request.getAttribute("courseId");
            } else {
                courseId = Integer.parseInt(request.getParameter("courseId"));
            }
        } catch (Exception e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid course ID.\"}");
            return;
        }

        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DBUtil.getConnection()) {

            // Check if already enrolled in course
            String checkEnrolledSql = "SELECT status FROM enrolled_courses WHERE student_id = ? AND course_id = ?";
            try (PreparedStatement checkEnrolledStmt = conn.prepareStatement(checkEnrolledSql)) {
                checkEnrolledStmt.setInt(1, studentId);
                checkEnrolledStmt.setInt(2, courseId);
                ResultSet rs = checkEnrolledStmt.executeQuery();
                if (rs.next()) {
                    result.put("success", false);
                    result.put("message", "You are already enrolled in this course.");
                    response.setContentType("application/json");
                    response.getWriter().write(new Gson().toJson(result));
                    return;
                }
            }
            // Check if a pending or approved request already exists
            String checkSql = "SELECT status FROM enrollment_requests WHERE student_id = ? AND course_id = ? AND status = 'PENDING'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, studentId);
                checkStmt.setInt(2, courseId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    result.put("success", false);
                    result.put("message", "You already have a pending request for this course.");
                    response.setContentType("application/json");
                    response.getWriter().write(new Gson().toJson(result));
                    return;
                }
            }
            // Insert new enrollment request
            String sql = "INSERT INTO enrollment_requests (student_id, course_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                stmt.executeUpdate();
                result.put("success", true);
                result.put("message", "Enrollment request submitted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(result));
    }

    private void unenrollFromCourse(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
            return;
        }

        Integer studentId = getStudentIdForUser((int) session.getAttribute("userId"));
        if (studentId == null) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"You are not registered as a student.\"}");
            return;
        }

        int courseId;
        try {
            courseId = (int) request.getAttribute("courseId");
        } catch (Exception e) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid course ID.\"}");
            return;
        }

        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "DELETE FROM enrolled_courses WHERE student_id = ? AND course_id = ?";
            String sql2 = "UPDATE enrollment_requests SET status = 'DROPPED' WHERE student_id = ? AND course_id = ?";
           
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                int updated = stmt.executeUpdate();
                
                if (updated > 0) {
                    result.put("success", true);
                    result.put("message", "Successfully unenrolled from course");
                } else {
                    result.put("success", false);
                    result.put("message", "Course not found or already dropped");
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setInt(1, studentId);
                stmt.setInt(2, courseId);
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    result.put("success", true);
                    result.put("message", "Successfully unenrolled from course");
                } else {
                    result.put("success", false);
                    result.put("message", "Course not found or already dropped");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(result));
    }

    private List<JsonObject> getPendingEnrollments(Connection conn, Integer studentId) throws SQLException {
        List<JsonObject> requests = new ArrayList<>();
        String sql = "SELECT er.id AS requestId, c.course_id, c.course_name, c.course_code, c.credits, er.status, er.request_date " +
                     "FROM enrollment_requests er " +
                     "JOIN courses c ON er.course_id = c.course_id " +
                     "WHERE er.student_id = ? AND er.status = 'PENDING'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JsonObject req = new JsonObject();
                    req.addProperty("requestId", rs.getLong("requestId"));
                    req.addProperty("courseId", rs.getInt("course_id"));
                    req.addProperty("courseName", rs.getString("course_name"));
                    req.addProperty("courseCode", rs.getString("course_code"));
                    req.addProperty("credits", rs.getInt("credits"));
                    req.addProperty("status", rs.getString("status"));
                    req.addProperty("requestDate", rs.getString("request_date"));
                    requests.add(req);
                }
            }
        }
        return requests;
    }

    //     private void cancelEnrollment(HttpServletRequest request, HttpServletResponse response) 
    //         throws IOException {
    //     HttpSession session = request.getSession(false);
    //     if (session == null || session.getAttribute("userId") == null) {
    //         response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not logged in");
    //         return;
    //     }

    //     int studentId = (int) session.getAttribute("userId");
    //     int courseId = Integer.parseInt(request.getParameter("courseId"));

    //     Map<String, Object> result = new HashMap<>();

    //     try (Connection conn = DBUtil.getConnection()) {
    //         String sql = "DELETE FROM enrolled_courses " +
    //                     "WHERE student_id = ? AND course_id = ? AND status = 'PENDING'";
            
    //         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
    //             stmt.setInt(1, studentId);
    //             stmt.setInt(2, courseId);
    //             int deleted = stmt.executeUpdate();
                
    //             if (deleted > 0) {
    //                 result.put("success", true);
    //                 result.put("message", "Successfully cancelled enrollment request");
    //             } else {
    //                 result.put("success", false);
    //                 result.put("message", "Enrollment request not found or already processed");
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         result.put("success", false);
    //         result.put("message", "Database error: " + e.getMessage());
    //     }

    //     response.setContentType("application/json");
    //     response.getWriter().write(gson.toJson(result));
    // }

    private void sendJsonResponse(HttpServletResponse response, List<JsonObject> data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }
} 