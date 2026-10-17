package edu.rentcar.service;

import edu.rentcar.enums.StatusReservation;
import edu.rentcar.model.Reservation;

import java.time.LocalDate;
import java.util.List;

public final class VehicleAvailabilityService {

    /**
     * Checks dates as [startDate, endDate).
     * The end date is exclusive.
     */
    public boolean isAvailable(
            String vehicleId,
            LocalDate startDate,
            LocalDate endDate,
            List<Reservation> reservations) {

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException(
                    "Vehicle ID is required.");
        }

        if (startDate == null || endDate == null
                || !startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                    "Invalid rental dates.");
        }

        if (reservations == null) {
            throw new IllegalArgumentException(
                    "Reservations are required.");
        }

        for (Reservation reservation : reservations) {

            boolean sameVehicle =
                    vehicleId.equals(reservation.getVehicleId());

            boolean confirmed =
                    StatusReservation.Confirmed.name()
                            .equals(reservation.getStatus());

            boolean datesOverlap =
                    startDate.isBefore(reservation.getEndDate())
                            && endDate.isAfter(reservation.getStartDate());

            if (sameVehicle && confirmed && datesOverlap) {
                return false;
            }
        }

        return true;
    }
}