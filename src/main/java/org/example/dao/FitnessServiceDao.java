
package org.example.dao;

import org.example.model.FitnessService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FitnessServiceDao {

    // Add fitness service
    public int addService(
            String serviceName,
            String description,
            double price
    ) throws SQLException {

        String sql = """
            INSERT INTO Fitness_Services
            (service_name, description, price)
            VALUES (?, ?, ?)
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, serviceName);
            statement.setString(2, description);
            statement.setDouble(3, price);

            statement.executeUpdate();

            try (ResultSet result =
                         connection.createStatement()
                                 .executeQuery("SELECT last_insert_rowid()")) {

                result.next();
                return result.getInt(1);
            }
        }
    }

    // View all fitness services
    public List<FitnessService> getAllServices()
            throws SQLException {

        List<FitnessService> services = new ArrayList<>();

        String sql = """
            SELECT * FROM Fitness_Services
            ORDER BY service_id DESC
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                services.add(new FitnessService(
                        result.getInt("service_id"),
                        result.getString("service_name"),
                        result.getString("description"),
                        result.getDouble("price")
                ));
            }
        }

        return services;
    }
}
