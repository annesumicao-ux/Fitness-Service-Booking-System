
package org.example.dao;

import org.example.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    private final ActivityLogDao activityLogDao;

    public BookingDao(ActivityLogDao activityLogDao) {
        this.activityLogDao = activityLogDao;
    }

    // Create a new booking
    public int createBooking(
            int customerId,
            int serviceId,
            int userId,
            String bookingDate,
            String bookingTime
    ) throws SQLException {

        String sql = """
            INSERT INTO Bookings
            (customer_id, service_id, user_id,
             booking_date, booking_time, booking_status)
            VALUES (?, ?, ?, ?, ?, 'PENDING')
            """;

        int bookingId;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);
            statement.setInt(2, serviceId);
            statement.setInt(3, userId);
            statement.setString(4, bookingDate);
            statement.setString(5, bookingTime);

            statement.executeUpdate();

            try (ResultSet result =
                         connection.createStatement()
                                 .executeQuery("SELECT last_insert_rowid()")) {

                result.next();
                bookingId = result.getInt(1);
            }
        }

        activityLogDao.logAction(
                userId,
                "BOOKING_CREATED",
                "Created booking ID: " + bookingId
        );

        return bookingId;
    }

    // View all bookings
    public List<Booking> getAllBookings() throws SQLException {

        List<Booking> bookings = new ArrayList<>();

        String sql = """
            SELECT * FROM Bookings
            ORDER BY booking_id DESC
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                bookings.add(new Booking(
                        result.getInt("booking_id"),
                        result.getInt("customer_id"),
                        result.getInt("service_id"),
                        result.getInt("user_id"),
                        result.getString("booking_date"),
                        result.getString("booking_time"),
                        result.getString("booking_status")
                ));
            }
        }

        return bookings;
    }

    // Update booking status
    public boolean updateBookingStatus(
            int bookingId,
            String newStatus,
            int actingUserId
    ) throws SQLException {

        String sql = """
            UPDATE Bookings
            SET booking_status = ?
            WHERE booking_id = ?
            """;

        boolean updated;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, newStatus);
            statement.setInt(2, bookingId);

            updated = statement.executeUpdate() > 0;
        }

        if (updated) {
            activityLogDao.logAction(
                    actingUserId,
                    "BOOKING_STATUS_UPDATED",
                    "Updated booking ID " + bookingId
                            + " to " + newStatus
            );
        }

        return updated;
    }
}
