
package edu.rentcar.service;

import edu.rentcar.enums.StatusReservation;
import edu.rentcar.model.Reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private final List<Reservation> reservations = new ArrayList<>();

    private final VehicleAvailabilityService availabilityService =
            new VehicleAvailabilityService();

    private int counter = 1;

    public Reservation createReservation(
            String customerId,
            String vehicleId,
            LocalDate startDate,
            LocalDate endDate) {

        String validCustomerId = requireText(customerId, "Customer ID");
        String validVehicleId = requireText(vehicleId, "Vehicle ID");

        if (!availabilityService.isAvailable(
                validVehicleId, startDate, endDate, reservations)) {
            throw new IllegalStateException(
                    "Vehicle is unavailable for these dates.");
        }

        Reservation reservation = new Reservation(
                "RES-" + counter++,
                validCustomerId,
                validVehicleId,
                startDate,
                endDate,
                StatusReservation.Pending.name()
        );

        reservations.add(reservation);
        return reservation;
    }

    public Reservation confirmReservation(String id) {
        Reservation pending = findReservation(id);

        if (!StatusReservation.Pending.name().equals(pending.getStatus())) {
            throw new IllegalStateException(
                    "Only pending reservations can be confirmed.");
        }

        // Exclude the reservation being confirmed.
        List<Reservation> otherReservations = reservations.stream()
                .filter(reservation -> !reservation.getId().equals(id))
                .toList();

        // Check availability again before changing the status.
        if (!availabilityService.isAvailable(
                pending.getVehicleId(),
                pending.getStartDate(),
                pending.getEndDate(),
                otherReservations)) {
            throw new IllegalStateException(
                    "Vehicle is no longer available.");
        }

        Reservation confirmed = new Reservation(
                pending.getId(),
                pending.getCustomerId(),
                pending.getVehicleId(),
                pending.getStartDate(),
                pending.getEndDate(),
                StatusReservation.Confirmed.name()
        );

        reservations.set(reservations.indexOf(pending), confirmed);

        return confirmed;
    }

    public Reservation findReservation(String id) {
        for (Reservation reservation : reservations) {
            if (reservation.getId().equals(id)) {
                return reservation;
            }
        }

        throw new IllegalArgumentException(
                "Reservation not found: " + id);
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    private static String requireText(String text, String field) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }

        return text.trim();
    }
}
