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

            try (ResultSet result = statement.executeQuery(
                    "SELECT sql FROM sqlite_master " +
                            "WHERE type = 'table' AND name = 'users'")) {

                if (result.next()) {
                    oldTableExists = true;
                    tableSql = result.getString("sql");
                }
            }

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
                    statement.execute("PRAGMA foreign_keys = ON");
                }
            }
        }

        System.out.println("Database is ready.");
    }
}