
package edu.rentcar.model;

public record BaseRentalCharge(
        RentalQuote quote,
        String vehicleDescription
) implements RentalCharge {

    public BaseRentalCharge {
        if (quote == null) {
            throw new IllegalArgumentException(
                    "Rental quote is required.");
        }

        if (vehicleDescription == null
                || vehicleDescription.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle description is required.");
        }

        if (quote.rentalDays() <= 0
                || !Double.isFinite(quote.rentalCost())
                || quote.rentalCost() < 0) {
            throw new IllegalArgumentException(
                    "Invalid rental quote.");
        }

        vehicleDescription = vehicleDescription.trim();
    }

    @Override
    public int rentalDays() {
        return quote.rentalDays();
    }

    @Override
    public double totalCost() {
        return quote.rentalCost();
    }

    @Override
    public String description() {
        return vehicleDescription
                + " rental (" + rentalDays() + " days)";
    }
}
