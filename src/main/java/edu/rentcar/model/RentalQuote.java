
package edu.rentcar.model;

public record RentalQuote(
        int rentalDays,
        double rentalCost,
        String coverageDescription,
        String assistanceDescription
) {}
