package com.squidzoo.quamcrawler;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnectionConfig {
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/stocksdb","root", "asdfasdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}
