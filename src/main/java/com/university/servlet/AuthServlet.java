package com.university.servlet;

import com.university.model.User;
import com.university.dao.UserDAO;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/login", "/signup", "/logout", "/check-auth"})
public class AuthServlet extends HttpServlet {
    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        switch (path) {
            case "/login":
                handleLogin(request, response);
                break;
            case "/signup":
                handleSignup(request, response);
                break;
            case "/logout":
                handleLogout(request, response);
                break;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        if (request.getServletPath().equals("/check-auth")) {
            checkAuth(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = userDAO.authenticate(email, password);
        Map<String, Object> result = new HashMap<>();

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole());

            result.put("success", true);
            result.put("role", user.getRole());
            if (user.getRole().equals("ADMIN")) {
                result.put("redirect", "/admin/dashboard.html");
            } else if (user.getRole().equals("INSTRUCTOR")) {
                result.put("redirect", "/instructor-dashboard.html");
            } else {
                result.put("redirect", "/client/dashboard.html");
            }
        } else {
            result.put("success", false);
            result.put("message", "Invalid email or password");
        }

        sendJsonResponse(response, result);
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        Map<String, Object> result = new HashMap<>();

        if (userDAO.emailExists(email)) {
            result.put("success", false);
            result.put("message", "Email already exists");
        } else {
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(password); // In production, hash the password
            user.setRole("CLIENT"); // Default role for new users

            if (userDAO.create(user)) {
                result.put("success", true);
                result.put("message", "Registration successful. Please login.");
            } else {
                result.put("success", false);
                result.put("message", "Registration failed. Please try again.");
            }
        }

        sendJsonResponse(response, result);
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        sendJsonResponse(response, result);
    }

    private void checkAuth(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            result.put("authenticated", true);
            result.put("role", user.getRole());
            result.put("name", user.getFirstName() + " " + user.getLastName());
        } else {
            result.put("authenticated", false);
        }

        sendJsonResponse(response, result);
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }
} 