package com.university;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import com.university.util.DBUtil;
import java.io.*;
import java.sql.*;
import com.university.dao.InstructorDAO;

@WebServlet("/instructors")
public class InstructorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM INSTRUCTORS")) {
            PrintWriter out = response.getWriter();
            out.print("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) out.print(",");
                out.print("{\"instructorId\":" + rs.getInt("instructor_id") +
                          ",\"firstName\":\"" + rs.getString("first_name") +
                          "\",\"lastName\":\"" + rs.getString("last_name") +
                          "\",\"email\":\"" + rs.getString("email") +
                          "\",\"department\":\"" + rs.getString("department") + "\"}");
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
        System.out.println("InstructorServlet doPost called");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String department = request.getParameter("department");

        InstructorDAO dao = new InstructorDAO();
        boolean success = dao.insertInstructorWithLowestAvailableId(firstName, lastName, email, department);
        if (success) {
            response.setStatus(201);
        } else {
            response.setStatus(500);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int instructorId = Integer.parseInt(request.getParameter("instructorId"));
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String department = request.getParameter("department");
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE INSTRUCTORS SET first_name=?, last_name=?, email=?, department=? WHERE instructor_id=?")) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, department);
            ps.setInt(5, instructorId);
            ps.executeUpdate();
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int instructorId = Integer.parseInt(request.getParameter("id"));
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM INSTRUCTORS WHERE instructor_id=?")) {
            ps.setInt(1, instructorId);
            ps.executeUpdate();
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
} 