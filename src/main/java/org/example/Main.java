package org.example;

import org.example.dao.DatabaseInitializer;
import org.example.dao.UserDao;
import org.example.ui.Authentication;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseInitializer initializer =
                    new DatabaseInitializer();

            initializer.initialize();

            UserDao userDao = new UserDao();

            try (Scanner scanner = new Scanner(System.in)) {
                Authentication authentication =
                        new Authentication(userDao, scanner);

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