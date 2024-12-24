package com.davodamc.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.davodamc.managers.YMLManager.*;

public class MySQLManager {
    private HikariDataSource dataSource;

    public void connect() {
        if (dataSource != null && !dataSource.isClosed()) {
            return; // Evitar configurar el pool de nuevo si ya está activo
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT + "/" + MYSQL_DATABASE + "?useSSL=false");
        config.setUsername(MYSQL_USERNAME);
        config.setPassword(MYSQL_PASSWORD);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10); // Máximo de conexiones en el pool

        dataSource = new HikariDataSource(config);
    }


    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public void insertPlayerClass(String playerName, String playerUUID, String playerClass) throws SQLException {
        String insertQuery = "INSERT INTO Clases (NOMBRE, UUID, CLASE) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, playerUUID);
            preparedStatement.setString(3, playerClass);
            preparedStatement.executeUpdate();
        }
    }

    public String getPlayerClass(String playerName) throws SQLException {
        String playerClass = null;
        String selectQuery = "SELECT CLASE FROM Clases WHERE NOMBRE = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

            preparedStatement.setString(1, playerName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    playerClass = resultSet.getString("CLASE");
                }
            }
        }
        return playerClass;
    }

    public void deletePlayerClass(String playerName, String playerUUID, String playerClass) throws SQLException {
        String deleteQuery = "DELETE FROM Clases WHERE NOMBRE = ? AND UUID = ? AND CLASE = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {

            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, playerUUID);
            preparedStatement.setString(3, playerClass);
            preparedStatement.executeUpdate();
        }
    }
}
