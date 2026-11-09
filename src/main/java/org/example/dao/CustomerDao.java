
package org.example.dao;

import org.example.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    // Register customer
    public int addCustomer(
            String customerName,
            String contactNumber,
            String email
    ) throws SQLException {

        String sql = """
            INSERT INTO Customers
            (customer_name, contact_number, email)
            VALUES (?, ?, ?)
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, customerName);
            statement.setString(2, contactNumber);
            statement.setString(3, email);

            statement.executeUpdate();

            try (ResultSet result =
                         connection.createStatement()
                                 .executeQuery("SELECT last_insert_rowid()")) {

                result.next();
                return result.getInt(1);
            }
        }
    }

    // View all customers
    public List<Customer> getAllCustomers() throws SQLException {

        List<Customer> customers = new ArrayList<>();

        String sql = """
            SELECT * FROM Customers
            ORDER BY customer_id DESC
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                customers.add(new Customer(
                        result.getInt("customer_id"),
                        result.getString("customer_name"),
                        result.getString("contact_number"),
                        result.getString("email")
                ));
            }
        }

        return customers;
    }
}
