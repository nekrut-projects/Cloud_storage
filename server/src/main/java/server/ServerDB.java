package server;

import lombok.extern.java.Log;
import shared.RegMessageRequest;

import java.sql.*;

@Log
public class ServerDB {
    static Connection connection;
    static Statement stmt;

    public void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server/src/main/resources/db/cloud_db.db");
        stmt = connection.createStatement();
    }

    public void disconnect() throws SQLException {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUserDir(String username, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT user_dir FROM users WHERE name=? AND password=?;");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("user_dir");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            log.warning("Error authorization - " + throwables.getMessage());
        }
        return null;
    }

    public String addUser(RegMessageRequest regMessage) throws SQLException {
        PreparedStatement pq = connection.prepareStatement("SELECT username FROM users WHERE username = ?;");
        pq.setString(1, regMessage.getUsername());
        ResultSet result = pq.executeQuery();

        if (result.getRow() != 0){
            return null;
        }

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, password, name, email, user_dir) VALUES (?, ?, ?, ?, ?);");
        ps.setString(1, regMessage.getUsername());
        ps.setString(2, regMessage.getPassword());
        ps.setString(3, regMessage.getName());
        ps.setString(4, regMessage.getEmail());
        ps.setString(5, regMessage.getUsername() + "_DIR");
        ps.executeUpdate();

        return regMessage.getUsername() + "_DIR";
    }
}
