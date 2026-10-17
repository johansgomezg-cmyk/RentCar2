
package edu.rentcar.service;

import edu.rentcar.enums.StatusReservation;
import edu.rentcar.model.Reservation;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private final ReservationService service =
            new ReservationService();

    private final LocalDate start =
            LocalDate.now().plusDays(2);

    @Test
    void createsPendingReservation() {
        Reservation reservation = service.createReservation(
                "CUS-1", "VEH-1", start, start.plusDays(3));

        assertEquals(
                StatusReservation.Pending.name(),
                reservation.getStatus()
        );

        assertEquals(1, service.getAllReservations().size());
    }

    @Test
    void allowsOverlappingPendingReservations() {
        service.createReservation(
                "CUS-1", "VEH-1", start, start.plusDays(4));

        service.createReservation(
                "CUS-2", "VEH-1",
                start.plusDays(1), start.plusDays(3));

        assertEquals(2, service.getAllReservations().size());
    }

    @Test
    void preventsConfirmingTwoOverlappingReservations() {
        Reservation first = service.createReservation(
                "CUS-1", "VEH-1", start, start.plusDays(4));

        Reservation second = service.createReservation(
                "CUS-2", "VEH-1",
                start.plusDays(1), start.plusDays(3));

        service.confirmReservation(first.getId());

        assertThrows(
                IllegalStateException.class,
                () -> service.confirmReservation(second.getId())
        );

        assertEquals(
                StatusReservation.Pending.name(),
                service.findReservation(second.getId()).getStatus()
        );
    }

    @Test
    void confirmedReservationBlocksNewRequest() {
        Reservation first = service.createReservation(
                "CUS-1", "VEH-1", start, start.plusDays(4));

        service.confirmReservation(first.getId());

        assertThrows(
                IllegalStateException.class,
                () -> service.createReservation(
                        "CUS-2", "VEH-1",
                        start.plusDays(2), start.plusDays(5))
        );
    }

    @Test
    void allowsAdjacentConfirmedReservations() {
        Reservation first = service.createReservation(
                "CUS-1", "VEH-1", start, start.plusDays(4));

        service.confirmReservation(first.getId());

        Reservation second = service.createReservation(
                "CUS-2", "VEH-1",
                start.plusDays(4), start.plusDays(6));

        Reservation confirmed = service.confirmReservation(second.getId());

        assertEquals(
                StatusReservation.Confirmed.name(),
                confirmed.getStatus()
        );
    }
}
