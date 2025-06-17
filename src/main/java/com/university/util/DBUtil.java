package com.university.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/universitydb?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "@pubge123@";
    
    static {
        try {
           Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException var1) {
           var1.printStackTrace();
        }
  
     }

    public static Connection getConnection() throws SQLException {
        System.out.println("DEBUG: Attempting DB connection to " + DB_URL + " as " + DB_USER);
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}