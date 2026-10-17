
package edu.rentcar.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a validated rental request.
 */
public final class RentalRequestBuilder {

    private String customerId;
    private String vehicleId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Rentalmode rentalMode;

    private final List<String> serviceCodes = new ArrayList<>();

    public RentalRequestBuilder customerId(String customerId) {
        this.customerId = customerId;
        return this;
    }

    public RentalRequestBuilder vehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
        return this;
    }

    public RentalRequestBuilder dates(
            LocalDate startDate,
            LocalDate endDate) {

        this.startDate = startDate;
        this.endDate = endDate;
        return this;
    }

    public RentalRequestBuilder rentalMode(Rentalmode rentalMode) {
        this.rentalMode = rentalMode;
        return this;
    }

    public RentalRequestBuilder addService(String serviceCode) {
        String code = requireText(serviceCode, "Service code");

        if (!serviceCodes.contains(code)) {
            serviceCodes.add(code);
        }

        return this;
    }

    public RentalRequest build() {

        String validCustomerId = requireText(customerId, "Customer ID");
        String validVehicleId = requireText(vehicleId, "Vehicle ID");

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start and end dates are required.");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Start date cannot be in the past.");
        }

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must be before end date.");
        }

        if (rentalMode == null) {
            throw new IllegalArgumentException(
                    "Rental mode is required.");
        }

        return new RentalRequest(
                validCustomerId,
                validVehicleId,
                startDate,
                endDate,
                rentalMode,
                serviceCodes
        );
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }

        return value.trim();
    }
}
