
package edu.rentcar.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Immutable rental request.
 * NO es una reservación congfirmada de momento.
 */
public final class RentalRequest {

    private final String customerId;
    private final String vehicleId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Rentalmode rentalMode;
    private final List<String> serviceCodes;

    RentalRequest(
            String customerId,
            String vehicleId,
            LocalDate startDate,
            LocalDate endDate,
            Rentalmode rentalMode,
            List<String> serviceCodes) {

        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rentalMode = rentalMode;
        this.serviceCodes = List.copyOf(serviceCodes);
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Rentalmode getRentalMode() {
        return rentalMode;
    }

    public List<String> getServiceCodes() {
        return serviceCodes;
    }
}
