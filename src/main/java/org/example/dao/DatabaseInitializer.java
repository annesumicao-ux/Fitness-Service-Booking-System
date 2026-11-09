
package org.example.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public void initialize() throws SQLException {

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement =
                     connection.createStatement()) {

            boolean oldTableExists = false;
            String tableSql = null;

            // Check if users table already exists
            try (ResultSet result = statement.executeQuery(
                    "SELECT sql FROM sqlite_master " +
                            "WHERE type = 'table' AND name = 'users'")) {

                if (result.next()) {
                    oldTableExists = true;
                    tableSql = result.getString("sql");
                }
            }

            // Create users table if it does not exist
            if (!oldTableExists) {

                statement.execute("""
                    CREATE TABLE users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        roles TEXT NOT NULL,
                        status TEXT NOT NULL DEFAULT 'PENDING'
                    )
                    """);

            } else if (tableSql != null
                    && tableSql.contains("'TEACHER'")
                    && tableSql.contains("'STUDENT'")) {

                // Migrate old users table
                statement.execute("PRAGMA foreign_keys = OFF");
                connection.setAutoCommit(false);

                try {

                    statement.execute("""
                        CREATE TABLE users_new (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password TEXT NOT NULL,
                            roles TEXT NOT NULL,
                            status TEXT NOT NULL DEFAULT 'PENDING'
                        )
                        """);

                    statement.execute("""
                        INSERT INTO users_new
                            (id, username, password, roles, status)
                        SELECT id, username, password, roles, status
                        FROM users
                        """);

                    statement.execute("DROP TABLE users");

                    statement.execute(
                            "ALTER TABLE users_new RENAME TO users"
                    );

                    connection.commit();

                } catch (SQLException e) {

                    connection.rollback();
                    throw e;

                } finally {

                    connection.setAutoCommit(true);
                }
            }

            // Enable foreign key support
            statement.execute("PRAGMA foreign_keys = ON");

            // Create activity logs table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS activity_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    description TEXT NOT NULL,
                    timestamp TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

                    FOREIGN KEY (user_id)
                    REFERENCES users(id)
                    ON DELETE CASCADE
                )
                """);

            // Create customers table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS Customers (
                    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_name TEXT NOT NULL,
                    contact_number TEXT,
                    email TEXT
                )
                """);

            // Create fitness services table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS Fitness_Services (
                    service_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    service_name TEXT NOT NULL,
                    description TEXT,
                    price REAL NOT NULL
                )
                """);

            // Create bookings table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS Bookings (
                    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER NOT NULL,
                    service_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    booking_date TIMESTAMP NOT NULL,
                    booking_time TEXT NOT NULL,
                    booking_status TEXT NOT NULL DEFAULT 'PENDING',

                    FOREIGN KEY (customer_id)
                    REFERENCES Customers(customer_id)
                    ON DELETE CASCADE,

                    FOREIGN KEY (service_id)
                    REFERENCES Fitness_Services(service_id)
                    ON DELETE CASCADE,

                    FOREIGN KEY (user_id)
                    REFERENCES users(id)
                    ON DELETE CASCADE
                )
                """);
        }

        System.out.println("Database is ready.");
        System.out.println("Activity Logs table is ready.");
        System.out.println("Customers table is ready.");
        System.out.println("Fitness Services table is ready.");
        System.out.println("Bookings table is ready.");
    }
}
