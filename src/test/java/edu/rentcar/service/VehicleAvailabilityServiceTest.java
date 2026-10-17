
package edu.rentcar.service;

import edu.rentcar.enums.StatusReservation;
import edu.rentcar.model.Reservation;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VehicleAvailabilityServiceTest {

    private final VehicleAvailabilityService service =
            new VehicleAvailabilityService();

    private final LocalDate start =
            LocalDate.now().plusDays(2);

    private Reservation reservation(
            String vehicleId, String status) {

        return new Reservation(
                "RES-1",
                "CUS-1",
                vehicleId,
                start.plusDays(2),
                start.plusDays(5),
                status
        );
    }

    @Test
    void vehicleIsAvailableWithoutReservations() {
        assertTrue(service.isAvailable(
                "VEH-1001",
                start,
                start.plusDays(3),
                List.of()
        ));
    }

    @Test
    void confirmedReservationBlocksOverlappingDates() {
        Reservation existing = reservation(
                "VEH-1001",
                StatusReservation.Confirmed.name()
        );

        assertFalse(service.isAvailable(
                "VEH-1001",
                start.plusDays(3),
                start.plusDays(6),
                List.of(existing)
        ));
    }

    @Test
    void anotherVehicleRemainsAvailable() {
        Reservation existing = reservation(
                "VEH-1001",
                StatusReservation.Confirmed.name()
        );

        assertTrue(service.isAvailable(
                "VEH-1002",
                start.plusDays(3),
                start.plusDays(6),
                List.of(existing)
        ));
    }

    @Test
    void pendingReservationDoesNotBlockDates() {
        Reservation existing = reservation(
                "VEH-1001",
                StatusReservation.Pending.name()
        );

        assertTrue(service.isAvailable(
                "VEH-1001",
                start.plusDays(3),
                start.plusDays(6),
                List.of(existing)
        ));
    }

    @Test
    void finalizedReservationDoesNotBlockDates() {
        Reservation existing = reservation(
                "VEH-1001",
                StatusReservation.Finalized.name()
        );

        assertTrue(service.isAvailable(
                "VEH-1001",
                start.plusDays(3),
                start.plusDays(6),
                List.of(existing)
        ));
    }

    @Test
    void rentalCanStartWhenPreviousRentalEnds() {
        Reservation existing = reservation(
                "VEH-1001",
                StatusReservation.Confirmed.name()
        );

        assertTrue(service.isAvailable(
                "VEH-1001",
                start.plusDays(5),
                start.plusDays(7),
                List.of(existing)
        ));
    }

    @Test
    void rentalCanEndWhenNextRentalStarts() {
        Reservation existing = reservation(
                "VEH-1001",
                StatusReservation.Confirmed.name()
        );

        assertTrue(service.isAvailable(
                "VEH-1001",
                start,
                start.plusDays(2),
                List.of(existing)
        ));
    }

    @Test
    void invalidDatesAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.isAvailable(
                        "VEH-1001",
                        start,
                        start,
                        List.of()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.isAvailable(
                        "VEH-1001",
                        start.plusDays(3),
                        start,
                        List.of()
                )
        );
    }

    @Test
    void missingVehicleIdIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.isAvailable(
                        null,
                        start,
                        start.plusDays(2),
                        List.of()
                )
        );
    }

    @Test
    void missingReservationListIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.isAvailable(
                        "VEH-1001",
                        start,
                        start.plusDays(2),
                        null
                )
        );
    }
}
