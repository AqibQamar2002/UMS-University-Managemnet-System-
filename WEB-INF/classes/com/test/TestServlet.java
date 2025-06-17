package com.test;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/TestServlet")
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Get the name parameter from the form
        String name = request.getParameter("name");
        
        // Set the content type
        response.setContentType("text/html");
        
        // Get the writer
        PrintWriter out = response.getWriter();
        
        // Write the response
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet Response</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }");
        out.println(".container { background-color: #f5f5f5; padding: 20px; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='container'>");
        out.println("<h1>Servlet Test Successful!</h1>");
        out.println("<p>Hello, " + name + "!</p>");
        out.println("<p>Your request was successfully processed by the servlet.</p>");
        out.println("<a href='index.html'>Back to form</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
} 