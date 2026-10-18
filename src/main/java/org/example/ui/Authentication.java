package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.Scanner;

public class Authentication {

    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

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
        System.out.println("1. Administrator");
        System.out.println("2. Receptionist");
        System.out.println("3. Staff");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();

        String role = switch (choice) {
            case "1" -> "ADMINISTRATOR";
            case "2" -> "RECEPTIONIST";
            case "3" -> "STAFF";
            default -> null;
        };

        if (role == null) {
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
            System.out.println(
                    "Login failed. Check your credentials and account status."
            );
            return;
        }

        System.out.println("Login successful!");
        System.out.println("Welcome, " + user.username() + "!");
        System.out.println("Role: " + user.roles());

        switch (user.roles()) {
            case "ADMINISTRATOR" ->
                    System.out.println("Administrator menu selected.");
            case "RECEPTIONIST" ->
                    System.out.println("Receptionist menu selected.");
            case "STAFF" ->
                    System.out.println("Staff menu selected.");
            default ->
                    System.out.println("Unknown role.");
        }

        System.out.println("Press Enter to log out.");
        scanner.nextLine();

        System.out.println("Logged out successfully.");
    }
}
