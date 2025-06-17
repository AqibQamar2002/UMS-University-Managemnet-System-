package com.university;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import com.university.util.DBUtil;
import java.io.*;
import java.sql.*;
import com.university.dao.StudentDAO;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("StudentServlet doGet called");
        response.setContentType("application/json");
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM STUDENTS")) {
            PrintWriter out = response.getWriter();
            out.print("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) out.print(",");
                out.print("{\"studentId\":" + rs.getInt("student_id") +
                          ",\"firstName\":\"" + rs.getString("first_name") +
                          "\",\"lastName\":\"" + rs.getString("last_name") +
                          "\",\"dateOfBirth\":\"" + rs.getString("date_of_birth") +
                          "\",\"email\":\"" + rs.getString("email") + "\"}");
                first = false;
            }
            out.print("]");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("StudentServlet doPost called");
        String methodOverride = request.getParameter("_method");
        if ("PUT".equalsIgnoreCase(methodOverride)) {
            doPut(request, response);
            return;
        }
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String dateOfBirth = request.getParameter("dateOfBirth");
        String email = request.getParameter("email");

        StudentDAO dao = new StudentDAO();
        boolean success = dao.insertStudentWithLowestAvailableId(firstName, lastName, dateOfBirth, email);
        if (success) {
            response.setStatus(201);
        } else {
            response.setStatus(500);
        }
    }
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("StudentServlet doPut called");
        // Print all parameters for debugging
        request.getParameterMap().forEach((k, v) -> System.out.println(k + " = " + java.util.Arrays.toString(v)));
        String studentIdStr = request.getParameter("studentId");
        System.out.println("studentIdStr: " + studentIdStr);
        if (studentIdStr == null || studentIdStr.trim().isEmpty()) {
            System.err.println("Error: studentId is null or empty");
            response.setStatus(400);
            return;
        }

        int studentId = Integer.parseInt(studentIdStr);
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String dateOfBirth = request.getParameter("dateOfBirth");
        String email = request.getParameter("email");
        
        System.out.println("Updating student with ID: " + studentId);
        System.out.println("New values - First Name: " + firstName + ", Last Name: " + lastName + 
                         ", DOB: " + dateOfBirth + ", Email: " + email);
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "UPDATE STUDENTS SET first_name=?, last_name=?, date_of_birth=?, email=? WHERE student_id=?")) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, dateOfBirth);
            ps.setString(4, email);
            ps.setInt(5, studentId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            if (rowsAffected == 0) {
                response.setStatus(404);
                return;
            }
            response.setStatus(200);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing studentId: " + e.getMessage());
            response.setStatus(400);
        } catch (SQLException e) {
            System.err.println("SQL Error in doPut: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
        } catch (Exception e) {
            System.err.println("Error in doPut: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("StudentServlet doDelete called");
        int studentId = Integer.parseInt(request.getParameter("id"));
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM STUDENTS WHERE student_id=?")) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
            response.setStatus(200);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }
} 