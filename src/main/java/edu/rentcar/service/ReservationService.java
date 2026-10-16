package edu.rentcar.service;
import edu.rentcar.enums.StatusReservation;
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
        if (!verificate(vehicleId, startDate, endDate)) {
            throw new IllegalStateException("El carro ya está reservado en esa fecha.");
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
    private boolean verificate(String vehicleId, LocalDate start, LocalDate finish) {
        for (Reservation r : reservations) {

            if (r.getVehicleId().equals(vehicleId)) {

                // ¿Las fechas se montan o chocan?
                boolean cross = start.isBefore(r.getEndDate()) && finish.isAfter(r.getStartDate());

                if (cross) {
                    return false;
                }
            }
        }
        return true;
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
        throw new IllegalArgumentException("No se encontró la reserva con ID: " + id);
    }
    public Reservation confirmReservation(String id) {
        Reservation reserva = findReservation(id);
        Reservation reservaConfirmada = new ReservationBuilder.Builder()
                .id(reserva.getId())
                .customerId(reserva.getCustomerId())
                .vehicleId(reserva.getVehicleId())
                .startDate(reserva.getStartDate())
                .endDate(reserva.getEndDate())
                .status(StatusReservation.Confirmed.name())
                .build();
        int index = reservations.indexOf(reserva);
        reservations.set(index, reservaConfirmada);
        return reservaConfirmada;
    }
}
