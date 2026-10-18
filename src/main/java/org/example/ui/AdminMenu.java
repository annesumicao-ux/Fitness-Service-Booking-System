package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;

    public AdminMenu(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n==================================");
            System.out.println("       ADMINISTRATOR MENU");
            System.out.println("==================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("0. Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> displayUsers(userDao.getAllUsers());
                case "2" -> displayUsers(userDao.getPendingUsers());
                case "3" -> approveAccount();
                case "0" -> {
                    running = false;
                    System.out.println("Administrator logged out.");
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void displayUsers(List<User> users) {
        System.out.println();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        printTable(users);
    }

    private void printTable(List<User> users) {
        String border = "------------------------------------------------------";

        System.out.println(border);
        System.out.printf(
                "%-5s %-15s %-17s %-10s%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );
        System.out.println(border);

        for (User user : users) {
            System.out.printf(
                    "%-5d %-15s %-17s %-10s%n",
                    user.id(),
                    shorten(user.username(), 15),
                    user.roles(),
                    user.status()
            );
        }

        System.out.println(border);
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() > maxLength) {
            return value.substring(0, maxLength);
        }

        return value;
    }

    private void approveAccount() {
        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("There are no pending registrations.");
            return;
        }

        System.out.println("\n--- PENDING REGISTRATIONS ---");
        printTable(pendingUsers);

        System.out.print("Enter User ID to approve (0 to cancel): ");
        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            System.out.println("Approval cancelled.");
            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID. Please enter a number.");
            return;
        }

        boolean exists = pendingUsers.stream()
                .anyMatch(user -> user.id() == userId);

        if (!exists) {
            System.out.println(
                    "User ID not found in the pending registrations."
            );
            return;
        }

        if (userDao.approveUser(userId)) {
            System.out.println(
                    "SUCCESS: User ID " + userId
                            + " has been approved!"
            );
        } else {
            System.out.println(
                    "Approval failed. Please check the database."
            );
        }
    }
}