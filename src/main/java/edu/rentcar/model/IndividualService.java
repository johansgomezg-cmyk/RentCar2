
package edu.rentcar.model;

public record IndividualService(
        String code,
        String name,
        double dailyRate
) implements RentalServiceItem {

    public IndividualService {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Service code is required.");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Service name is required.");
        }

        if (!Double.isFinite(dailyRate) || dailyRate < 0) {
            throw new IllegalArgumentException(
                    "Daily rate must be non-negative.");
        }

        code = code.trim();
        name = name.trim();
    }

    @Override
    public String description() {
        return name;
    }

    @Override
    public double costForDays(int rentalDays) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException(
                    "Rental days must be positive.");
        }

        double cost = dailyRate * rentalDays;

        if (!Double.isFinite(cost)) {
            throw new IllegalStateException(
                    "Service cost is too large.");
        }

        return cost;
    }
}
