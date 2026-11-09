
package org.example.ui;

import org.example.dao.UserDao;
import org.example.dao.ActivityLogDao;
import org.example.model.User;
import org.example.model.ActivityLog;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(
            UserDao userDao,
            ActivityLogDao activityLogDao,
            Scanner scanner,
            User currentAdmin
    ) {
        this.userDao = userDao;
        this.activityLogDao = activityLogDao;
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
            System.out.println("5. View System Audit Logs");
            System.out.println("0. Logout");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {

                case 1 ->
                        displayUsers(userDao.getAllUsers());

                case 2 ->
                        displayUsers(userDao.getPendingUsers());

                case 3 ->
                        approveAccount();

                case 4 ->
                        updateUser();

                case 5 ->
                        auditLogMenu();

                case 0 -> {
                    running = false;
                    System.out.println("Administrator logged out.");
                }

                default ->
                        System.out.println(
                                "Invalid choice. Please enter 0 to 5."
                        );
            }
        }
    }

    // Display users
    private void displayUsers(List<User> users) {

        System.out.println();

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        printTable(users);
    }

    // Print user table
    private void printTable(List<User> users) {

        String border =
                "---------------------------------------------------------------";

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

    // Approve user account
    private void approveAccount() {

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println(
                    "There are no pending registrations."
            );
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

        if (userDao.approveUser(userId, currentAdmin.id())) {

            System.out.println(
                    "SUCCESS: User ID " + userId +
                            " has been approved!"
            );

        } else {

            System.out.println(
                    "Approval failed. Please check the database."
            );
        }
    }

    // Update user role and status
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

        System.out.println(
                "Selected username: " + targetUser.username()
        );

        System.out.println(
                "Current role: " + targetUser.roles()
        );

        System.out.println(
                "Current status: " + targetUser.status()
        );

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

            System.out.print(
                    "Proceed? (Y/N or 0 to cancel): "
            );

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
                userId,
                newRole,
                newStatus,
                currentAdmin.id()
        )) {

            System.out.println(
                    "SUCCESS: User ID " + userId +
                            " role and status updated successfully!"
            );

        } else {

            System.out.println(
                    "Update failed. Please check the database."
            );
        }

        System.out.println("\n--- UPDATED USER LIST ---");

        displayUsers(userDao.getAllUsers());
    }

    // Validate integer input
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

    // Validate user role
    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable roles:");
            System.out.println("1. ADMINISTRATOR");
            System.out.println("2. RECEPTIONIST");
            System.out.println("3. STAFF");

            System.out.print(
                    "Enter new role (or 0 to cancel): "
            );

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            String role = switch (input) {
                case "1" -> "ADMINISTRATOR";
                case "2" -> "RECEPTIONIST";
                case "3" -> "STAFF";
                default -> null;
            };

            if (role != null) {
                return role;
            }

            System.out.println(
                    "Invalid role. Please enter 1, 2, or 3."
            );
        }
    }

    // Validate user status
    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");

            System.out.print(
                    "Enter new status (or 0 to cancel): "
            );

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            String status = switch (input) {
                case "1" -> "APPROVED";
                case "2" -> "PENDING";
                default -> null;
            };

            if (status != null) {
                return status;
            }

            System.out.println(
                    "Invalid status. Please enter 1 or 2."
            );
        }
    }

    // System Audit Logs Menu
    private void auditLogMenu() {

        boolean running = true;

        while (running) {

            System.out.println("\n==================================");
            System.out.println("       SYSTEM AUDIT LOGS");
            System.out.println("==================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("0. Back to Administrator Menu");

            int choice = readIntInput("Enter choice: ");

            switch (choice) {

                case 1 ->
                        viewAllLogs();

                case 2 ->
                        filterLogsByUserId();

                case 3 ->
                        clearHistoricalLogs();

                case 0 ->
                        running = false;

                default ->
                        System.out.println("Invalid choice.");
            }
        }
    }

    // View all activity logs
    private void viewAllLogs() {

        try {

            List<ActivityLog> logs = activityLogDao.getAllLogs();

            displayLogs(logs);

        } catch (SQLException e) {

            System.out.println(
                    "Unable to retrieve activity logs: " + e.getMessage()
            );
        }
    }

    // Filter logs by user ID
    private void filterLogsByUserId() {

        int userId = readIntInput(
                "Enter User ID to filter (0 to cancel): "
        );

        if (userId == 0) {
            System.out.println("Filtering cancelled.");
            return;
        }

        if (userId < 0) {
            System.out.println("User ID must be positive.");
            return;
        }

        try {

            List<ActivityLog> logs =
                    activityLogDao.getLogsByUserId(userId);

            System.out.println(
                    "\n--- ACTIVITY LOGS FOR USER ID " + userId + " ---"
            );

            displayLogs(logs);

        } catch (SQLException e) {

            System.out.println(
                    "Unable to filter activity logs: " + e.getMessage()
            );
        }
    }

    // Display activity logs in table format
    private void displayLogs(List<ActivityLog> logs) {

        if (logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        String border =
                "----------------------------------------------------------------------------------------------------------";

        System.out.println(border);

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "ID",
                "USER ID",
                "ACTION TYPE",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(border);

        for (ActivityLog log : logs) {

            System.out.printf(
                    "%-5d | %-8d | %-18s | %-35s | %-20s%n",
                    log.id(),
                    log.userId(),
                    shorten(log.actionType(), 18),
                    shorten(log.description(), 35),
                    shorten(log.timestamp(), 20)
            );
        }

        System.out.println(border);
    }

    // Clear historical activity logs
    private void clearHistoricalLogs() {

        System.out.print(
                "Are you sure you want to clear all historical logs? (Y/N): "
        );

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Clear operation cancelled.");
            return;
        }

        try {

            int deletedRows = activityLogDao.clearAllLogs();

            System.out.println(
                    deletedRows + " historical log(s) cleared successfully."
            );

        } catch (SQLException e) {

            System.out.println(
                    "Unable to clear activity logs: " + e.getMessage()
            );
        }
    }
}
