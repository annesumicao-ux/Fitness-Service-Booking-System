package org.example.dao;

import org.example.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final ActivityLogDao activityLogDao =
            new ActivityLogDao();

    // 1. Register a new user
    public boolean register(
            String username,
            String password,
            String role
    ) {
        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role.trim().toUpperCase());

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            System.out.println(
                    "Registration failed: " + e.getMessage()
            );
            return false;
        }
    }

    // 2. Login an approved user
    public User login(String username, String password) {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                  AND password = ?
                  AND status = 'APPROVED'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Login failed: " + e.getMessage()
            );
        }

        return null;
    }

    // 3. Get all registered users
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to retrieve users: " + e.getMessage()
            );
        }

        return users;
    }

    // 4. Get pending user registrations
    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE status = 'PENDING'
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(new User(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("password"),
                        result.getString("roles"),
                        result.getString("status")
                ));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to retrieve pending users: "
                            + e.getMessage()
            );
        }

        return users;
    }

    // 5. Approve user and record activity
    public boolean approveUser(int userId, int adminId) {

        String sql = """
                UPDATE users
                SET status = 'APPROVED'
                WHERE id = ?
                  AND status = 'PENDING'
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            int rows = statement.executeUpdate();

            if (rows == 1) {

                activityLogDao.logAction(
                        adminId,
                        "USER_APPROVED",
                        "Admin " + adminId +
                                " approved User " + userId
                );

                System.out.println(
                        "User approval successfully logged."
                );

                return true;
            }

            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Failed to approve user: " + e.getMessage()
            );
            return false;
        }
    }

    // 6. Update user role and status and record activity
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus,
            int adminId
    ) {

        String sql = """
                UPDATE users
                SET roles = ?, status = ?
                WHERE id = ?
                """;

        String role = newRole.trim().toUpperCase();
        String status = newStatus.trim().toUpperCase();

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, role);
            statement.setString(2, status);
            statement.setInt(3, userId);

            int rows = statement.executeUpdate();

            if (rows == 1) {

                activityLogDao.logAction(
                        adminId,
                        "USER_UPDATED",
                        "Admin " + adminId +
                                " changed User " + userId +
                                " role to " + role +
                                " and status to " + status
                );

                System.out.println(
                        "User update successfully logged."
                );

                return true;
            }

            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Failed to update user: " + e.getMessage()
            );
            return false;
        }
    }

    // 7. Find user by username
    public User findByUsername(String username) {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new User(
                            result.getInt("id"),
                            result.getString("username"),
                            result.getString("password"),
                            result.getString("roles"),
                            result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to find user: " + e.getMessage()
            );
        }

        return null;
    }
}