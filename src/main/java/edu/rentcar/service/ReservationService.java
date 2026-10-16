package edu.rentcar.service;

import edu.rentcar.model.ReservationBuilder;
import edu.rentcar.model.Reservation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    private final List<Reservation> reservations = new ArrayList<>();
    private int counter = 1;
    public Reservation createReservation(String customerId, String vehicleId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        }
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser antes de finalizar.");
        }
        String reservationId = "RES-" + counter++;
        Reservation reservation = new ReservationBuilder.Builder()
                .id(reservationId)
                .customerId(customerId)
                .vehicleId(vehicleId)
                .startDate(startDate)
                .endDate(endDate)
                .status("PENDING")
                .build();
        reservations.add(reservation);
        return reservation;
    }
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public Reservation findReservation(String id) {
        for (Reservation r : reservations) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        throw new IllegalArgumentException("No se encontro la reserva con ID: " + id);
    }
}