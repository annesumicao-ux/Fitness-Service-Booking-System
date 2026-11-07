package org.example;

import org.example.dao.DatabaseInitializer;
import org.example.dao.UserDao;
import org.example.dao.ActivityLogDao;
import org.example.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            // Initialize database
            DatabaseInitializer initializer =
                    new DatabaseInitializer();

            initializer.initialize();

            // Initialize DAOs
            UserDao userDao = new UserDao();
            ActivityLogDao activityLogDao = new ActivityLogDao();

            // Start application
            try (Scanner scanner = new Scanner(System.in)) {

                Authentication authentication =
                        new Authentication(
                                userDao,
                                activityLogDao,
                                scanner
                        );

                authentication.start();
            }

        } catch (SQLException e) {

            System.err.println(
                    "Database initialization failed: "
                            + e.getMessage()
            );
        }
    }
}