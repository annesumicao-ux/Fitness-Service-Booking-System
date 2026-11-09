
package org.example.model;

public record FitnessService(
        int serviceId,
        String serviceName,
        String description,
        double price
) {
}
