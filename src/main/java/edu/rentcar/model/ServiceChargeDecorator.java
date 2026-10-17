
package edu.rentcar.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ServiceChargeDecorator implements RentalCharge {

    private final RentalCharge wrappedCharge;
    private final RentalServiceItem addedService;

    public ServiceChargeDecorator(
            RentalCharge wrappedCharge,
            RentalServiceItem addedService) {

        if (wrappedCharge == null || addedService == null) {
            throw new IllegalArgumentException(
                    "Rental charge and service are required.");
        }

        this.wrappedCharge = wrappedCharge;
        this.addedService = addedService;
    }

    @Override
    public int rentalDays() {
        return wrappedCharge.rentalDays();
    }

    @Override
    public double totalCost() {

        double baseCost = wrappedCharge.totalCost();

        double serviceCost = addedService.costForDays(
                rentalDays()
        );

        double total = baseCost + serviceCost;

        if (!Double.isFinite(total) || total < 0) {
            throw new IllegalStateException(
                    "Invalid total rental cost.");
        }

        return BigDecimal.valueOf(total)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public String description() {
        return wrappedCharge.description()
                + " + "
                + addedService.description();
    }
}
