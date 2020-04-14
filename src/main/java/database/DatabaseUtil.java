package database;

import java.sql.*;

public class DatabaseUtil {

    public static Connection getConnection(String url, String username, String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static ResultSet executeQuery(String query) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Statement statement = getConnection("jdbc:mysql://localhost:3308/traffic_limits", "root", "helloworld").createStatement();
        return statement.executeQuery(query);
    }


    public static Long getMinLimit() throws SQLException, ClassNotFoundException {
        String query = "SELECT limit_value FROM limits_per_hour " +
                "WHERE limit_name = 'min' " +
                "ORDER BY effective_date desc " +
                "LIMIT 1";
        ResultSet result = executeQuery(query);
        result.next();
        return result.getLong(1);
    }

    public static Long getMaxLimit() throws SQLException, ClassNotFoundException {
        String query = "SELECT limit_value FROM limits_per_hour " +
                "WHERE limit_name = 'max' " +
                "ORDER BY effective_date desc " +
                "LIMIT 1";
        ResultSet result = executeQuery(query);
        result.next();
        return result.getLong(1);
    }

}
