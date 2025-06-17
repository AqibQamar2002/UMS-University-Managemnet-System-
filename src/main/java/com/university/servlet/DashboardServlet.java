package com.university.servlet;

import com.university.dao.StudentDAO;
import com.university.dao.CourseDAO;
import com.university.dao.InstructorDAO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/dashboard-stats"})
public class DashboardServlet extends HttpServlet {
    private StudentDAO studentDAO = new StudentDAO();
    private CourseDAO courseDAO = new CourseDAO();
    private InstructorDAO instructorDAO = new InstructorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        int totalStudents = studentDAO.getCount();
        int totalCourses = courseDAO.getCount();
        int totalInstructors = instructorDAO.getCount();

        PrintWriter out = resp.getWriter();
        out.print("{" +
                "\"totalStudents\":" + totalStudents + "," +
                "\"totalCourses\":" + totalCourses + "," +
                "\"totalInstructors\":" + totalInstructors +
                "}");
        out.flush();
    }
} 