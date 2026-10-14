package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.Scanner;

public class MenuUI {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n==================================");
            System.out.println(" FITNESS SERVICE BOOKING SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    running = false;
                    System.out.println("Application closed.");
                }
                default ->
                        System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER ACCOUNT ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Fields cannot be empty.");
            return;
        }

        System.out.println("Select role:");
        System.out.println("1. TEACHER");
        System.out.println("2. STUDENT");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();
        String role;

        if (choice.equals("1")) {
            role = "TEACHER";
        } else if (choice.equals("2")) {
            role = "STUDENT";
        } else {
            System.out.println("Invalid role.");
            return;
        }

        if (userDao.register(username, password, role)) {
            System.out.println("Registration successful!");
            System.out.println("Role: " + role);
            System.out.println("Status: PENDING");
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Incorrect username or password.");
            return;
        }

        System.out.println("Login successful!");
        System.out.println("Welcome, " + user.username() + "!");
        System.out.println("Role: " + user.roles());
        System.out.println("Account status: " + user.status());

        System.out.println("Press Enter to log out.");
        scanner.nextLine();

        System.out.println("Logged out successfully.");
    }
}