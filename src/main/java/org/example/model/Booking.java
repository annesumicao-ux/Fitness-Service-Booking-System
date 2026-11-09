
package org.example.model;

public record Booking(
        int bookingId,
        int customerId,
        int serviceId,
        int userId,
        String bookingDate,
        String bookingTime,
        String bookingStatus
) {
}
