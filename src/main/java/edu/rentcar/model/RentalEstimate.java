
package edu.rentcar.model;

public record RentalEstimate(
        String vehicleId,
        int rentalDays,
        double totalCost,
        String description,
        String coverageDescription,
        String assistanceDescription
) {
}
