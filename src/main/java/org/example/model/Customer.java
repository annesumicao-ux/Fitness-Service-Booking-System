
package org.example.model;

public record Customer(
        int customerId,
        String customerName,
        String contactNumber,
        String email
) {
}
