
package edu.rentcar.service;

import edu.rentcar.enums.StatusReservation;
import edu.rentcar.model.Reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {

    private final List<Reservation> reservations =
            new ArrayList<>();

    private final VehicleAvailabilityService availabilityService =
            new VehicleAvailabilityService();

    private int counter = 1;

    public synchronized Reservation createReservation(
            String customerId,
            String vehicleId,
            LocalDate startDate,
            LocalDate endDate) {

        String validCustomerId =
                requireText(customerId, "Customer ID");

        String validVehicleId =
                requireText(vehicleId, "Vehicle ID");

        if (!availabilityService.isAvailable(
                validVehicleId,
                startDate,
                endDate,
                reservations)) {

            throw new IllegalStateException(
                    "Vehicle is unavailable.");
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

    // Package-private: not directly accessible to UI controllers.
    synchronized Reservation confirmReservation(String id) {

        Reservation pending = findReservation(id);

        if (!StatusReservation.Pending.name()
                .equals(pending.getStatus())) {

            throw new IllegalStateException(
                    "Only pending reservations can be confirmed.");
        }

        List<Reservation> others = reservations.stream()
                .filter(r -> !r.getId().equals(id))
                .toList();

        if (!availabilityService.isAvailable(
                pending.getVehicleId(),
                pending.getStartDate(),
                pending.getEndDate(),
                others)) {

            throw new IllegalStateException(
                    "Vehicle is no longer available.");
        }

        return replaceStatus(
                pending, StatusReservation.Confirmed);
    }

    // Records a failed operation without confirming the reservation.
    synchronized Reservation markUnsuccessful(
            String id,
            StatusReservation failureStatus) {

        if (failureStatus != StatusReservation.PaymentDeclined
                && failureStatus != StatusReservation.PaymentError) {

            throw new IllegalArgumentException(
                    "Invalid failure status.");
        }

        Reservation pending = findReservation(id);

        if (!StatusReservation.Pending.name()
                .equals(pending.getStatus())) {

            throw new IllegalStateException(
                    "Only pending reservations can be updated.");
        }

        return replaceStatus(pending, failureStatus);
    }

    private Reservation replaceStatus(
            Reservation current,
            StatusReservation newStatus) {

        Reservation updated = new Reservation(
                current.getId(),
                current.getCustomerId(),
                current.getVehicleId(),
                current.getStartDate(),
                current.getEndDate(),
                newStatus.name()
        );

        int index = reservations.indexOf(current);
        reservations.set(index, updated);

        return updated;
    }

    public synchronized Reservation findReservation(String id) {

        for (Reservation reservation : reservations) {
            if (reservation.getId().equals(id)) {
                return reservation;
            }
        }

        throw new IllegalArgumentException(
                "Reservation not found: " + id);
    }

    public synchronized List<Reservation> getAllReservations() {
        return List.copyOf(reservations);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required.");
        }

        return value.trim();
    }
}
