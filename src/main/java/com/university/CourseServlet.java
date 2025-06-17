package com.university;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import com.university.util.DBUtil;
import java.io.*;
import java.sql.*;
import com.university.dao.CourseDAO;

@WebServlet("/courses")
public class CourseServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM COURSES")) {
            PrintWriter out = response.getWriter();
            out.print("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) out.print(",");
                out.print("{\"courseId\":" + rs.getInt("course_id") +
                          ",\"courseName\":\"" + rs.getString("course_name") +
                          "\",\"courseCode\":\"" + rs.getString("course_code") +
                          "\",\"credits\":" + rs.getInt("credits") + "}");
                first = false;
            }
            out.print("]");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("CourseServlet doPost called");
        String courseName = request.getParameter("courseName");
        String courseCode = request.getParameter("courseCode");
        int credits = Integer.parseInt(request.getParameter("credits"));

        CourseDAO dao = new CourseDAO();
        boolean success = dao.insertCourseWithLowestAvailableId(courseName, courseCode, credits);
        if (success) {
            response.setStatus(201);
        } else {
            response.setStatus(500);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int courseId = Integer.parseInt(request.getParameter("courseId"));
        String courseName = request.getParameter("courseName");
        String courseCode = request.getParameter("courseCode");
        int credits = Integer.parseInt(request.getParameter("credits"));
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE COURSES SET course_name=?, course_code=?, credits=? WHERE course_id=?")) {
            ps.setString(1, courseName);
            ps.setString(2, courseCode);
            ps.setInt(3, credits);
            ps.setInt(4, courseId);
            ps.executeUpdate();
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int courseId = Integer.parseInt(request.getParameter("id"));
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM COURSES WHERE course_id=?")) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
} 