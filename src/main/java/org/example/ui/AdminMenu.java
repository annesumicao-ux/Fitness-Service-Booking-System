package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(
            UserDao userDao,
            Scanner scanner,
            User currentAdmin
    ) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
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
            System.out.println("4. Update User Role and Status");
            System.out.println("0. Logout");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {
                case 1 -> displayUsers(userDao.getAllUsers());
                case 2 -> displayUsers(userDao.getPendingUsers());
                case 3 -> approveAccount();
                case 4 -> updateUser();
                case 0 -> {
                    running = false;
                    System.out.println("Administrator logged out.");
                }
                default -> System.out.println(
                        "Invalid choice. Please enter 0 to 4."
                );
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
        String border =
                "----------------------------------------------------------------";

        System.out.println(border);
        System.out.printf(
                "%-5s %-20s %-20s %-12s%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );
        System.out.println(border);

        for (User user : users) {
            System.out.printf(
                    "%-5d %-20s %-20s %-12s%n",
                    user.id(),
                    shorten(user.username(), 20),
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

        int userId = readIntInput(
                "Enter User ID to approve (0 to cancel): "
        );

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        if (userId < 0) {
            System.out.println("User ID must be positive.");
            return;
        }

        boolean exists = pendingUsers.stream()
                .anyMatch(user -> user.id() == userId);

        if (!exists) {
            System.out.println(
                    "User ID not found in pending registrations."
            );
            return;
        }

        if (userDao.approveUser(userId)) {
            System.out.println(
                    "SUCCESS: User ID " + userId + " has been approved!"
            );
        } else {
            System.out.println(
                    "Approval failed. Please check the database."
            );
        }
    }

    private void updateUser() {
        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users available to update.");
            return;
        }

        System.out.println("\n--- UPDATE USER ROLE AND STATUS ---");
        printTable(users);

        int userId = readIntInput(
                "Enter User ID to update (0 to cancel): "
        );

        if (userId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userId < 0) {
            System.out.println("User ID must be positive.");
            return;
        }

        // Prevent editing the currently logged-in admin.
        if (userId == currentAdmin.id()) {
            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted."
            );
            return;
        }

        User targetUser = users.stream()
                .filter(user -> user.id() == userId)
                .findFirst()
                .orElse(null);

        if (targetUser == null) {
            System.out.println("User ID not found.");
            return;
        }

        System.out.println("Selected username: " + targetUser.username());
        System.out.println("Current role: " + targetUser.roles());
        System.out.println("Current status: " + targetUser.status());
        System.out.println("Enter 0 at any prompt to cancel.");

        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        System.out.println("\nConfirm the following update:");
        System.out.println("User ID: " + userId);
        System.out.println("New role: " + newRole);
        System.out.println("New status: " + newStatus);

        String confirmation;

        while (true) {
            System.out.print("Proceed? (Y/N): ");
            confirmation = scanner.nextLine().trim();

            if (confirmation.equalsIgnoreCase("Y")
                    || confirmation.equalsIgnoreCase("N")) {
                break;
            }

            if (confirmation.equals("0")) {
                System.out.println("Update cancelled.");
                return;
            }

            System.out.println("Enter Y, N, or 0 to cancel.");
        }

        if (confirmation.equalsIgnoreCase("N")) {
            System.out.println("Update cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(
                userId, newRole, newStatus)) {
            System.out.println(
                    "SUCCESS: User ID " + userId
                            + " role and status updated successfully!"
            );
        } else {
            System.out.println(
                    "Update failed. Please check the database."
            );
        }

        System.out.println("\n--- UPDATED USER LIST ---");
        displayUsers(userDao.getAllUsers());
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter a whole number."
                );
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("\nAvailable roles:");
            System.out.println("1. ADMINISTRATOR");
            System.out.println("2. RECEPTIONIST");
            System.out.println("3. STAFF");
            System.out.print("Enter new role (or 0 to cancel): ");

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            String role = input.toUpperCase();

            if (role.equals("ADMINISTRATOR")
                    || role.equals("RECEPTIONIST")
                    || role.equals("STAFF")) {
                return role;
            }

            System.out.println(
                    "Invalid role. Enter ADMINISTRATOR, RECEPTIONIST, or STAFF."
            );
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.println("\nAvailable statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.print("Enter new status (or 0 to cancel): ");

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            String status = input.toUpperCase();

            if (status.equals("APPROVED")
                    || status.equals("PENDING")) {
                return status;
            }

            System.out.println(
                    "Invalid status. Enter APPROVED or PENDING."
            );
        }
    }
}