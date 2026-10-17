
package edu.rentcar.service;

import edu.rentcar.data.SampleData;
import edu.rentcar.model.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentalFacadeTest {

    private final ReservationService reservations =
            new ReservationService();

    private final RentalFacade facade = new RentalFacade(
            SampleData.vehicles(),
            reservations,
            new RentalPricingService(),
            new VehicleAvailabilityService(),
            RentalServiceCatalog.standardServices()
    );

    private final LocalDate start =
            LocalDate.now().plusDays(2);

    private RentalRequest request(
            String vehicleId,
            Rentalmode mode,
            String... services) {

        RentalRequestBuilder builder = new RentalRequestBuilder()
                .customerId("CUS-1001")
                .vehicleId(vehicleId)
                .dates(start, start.plusDays(3))
                .rentalMode(mode);

        for (String service : services) {
            builder.addService(service);
        }

        return builder.build();
    }

    @Test
    void calculatesBaseRentalWithoutServices() {
        RentalEstimate estimate = facade.quote(
                request("VEH-1001", new Economy())
        );

        assertEquals(3, estimate.rentalDays());
        assertEquals(450000, estimate.totalCost(), 0.01);
    }

    @Test
    void calculatesRentalWithMultipleServices() {
        RentalEstimate estimate = facade.quote(
                request(
                        "VEH-1001",
                        new Economy(),
                        "GPS",
                        "CHILD_SEAT"
                )
        );

        assertEquals(510000, estimate.totalCost(), 0.01);
        assertTrue(estimate.description().contains("GPS"));
        assertTrue(estimate.description().contains("Child seat"));
    }

    @Test
    void integratesAbstractFactoryWithComposite() {
        RentalEstimate estimate = facade.quote(
                request(
                        "VEH-1001",
                        new Executive(),
                        "COMPLETE_PACK"
                )
        );

        assertEquals(609000, estimate.totalCost(), 0.01);
        assertTrue(estimate.description().contains("Complete pack"));
    }

    @Test
    void rejectsUnknownVehicle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> facade.quote(
                        request("VEH-9999", new Economy())
                )
        );
    }

    @Test
    void rejectsUnknownService() {
        assertThrows(
                IllegalArgumentException.class,
                () -> facade.quote(
                        request(
                                "VEH-1001",
                                new Economy(),
                                "UNKNOWN_SERVICE"
                        )
                )
        );
    }

    @Test
    void rejectsUnavailableVehicle() {
        Reservation existing = reservations.createReservation(
                "CUS-1002",
                "VEH-1001",
                start,
                start.plusDays(3)
        );

        reservations.confirmReservation(existing.getId());

        assertThrows(
                IllegalStateException.class,
                () -> facade.quote(
                        request("VEH-1001", new Economy())
                )
        );
    }

    @Test
    void preventsChargingTheSameServiceTwice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> facade.quote(
                        request(
                                "VEH-1001",
                                new Economy(),
                                "COMPLETE_PACK",
                                "GPS"
                        )
                )
        );
    }
}
