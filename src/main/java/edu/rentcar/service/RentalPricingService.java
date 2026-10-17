
package edu.rentcar.service;

import edu.rentcar.model.RentalQuote;
import edu.rentcar.model.RentalRequest;
import edu.rentcar.model.Rentalmode;
import edu.rentcar.model.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

public final class RentalPricingService {

    public RentalQuote calculateQuote(
            RentalRequest request,
            Vehicle vehicle) {

        if (request == null || vehicle == null) {
            throw new IllegalArgumentException(
                    "Request and vehicle are required.");
        }

        if (!request.getVehicleId().equals(vehicle.getId())) {
            throw new IllegalArgumentException(
                    "Vehicle does not match request.");
        }

        double dailyRate = vehicle.getDailyRate();

        if (!Double.isFinite(dailyRate) || dailyRate <= 0) {
            throw new IllegalArgumentException(
                    "Daily rate must be positive.");
        }

        long days = ChronoUnit.DAYS.between(
                request.getStartDate(),
                request.getEndDate()
        );

        if (days <= 0 || days > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Invalid rental period.");
        }

        // Obtain policies from the selected factory.
        Rentalmode mode = request.getRentalMode();

        double cost = mode.createTariffPolicy()
                .calculateCost(dailyRate, (int) days);

        if (!Double.isFinite(cost) || cost < 0) {
            throw new IllegalStateException(
                    "Invalid rental cost.");
        }

        double roundedCost = BigDecimal.valueOf(cost)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        return new RentalQuote(
                (int) days,
                roundedCost,
                mode.createCoveragePolicy().gettextcoverage(),
                mode.createAssistancePolicy().gettext()
        );
    }
}
