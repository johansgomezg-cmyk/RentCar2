
package edu.rentcar.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RentalRequestBuilderTest {

    private final LocalDate start = LocalDate.now().plusDays(2);
    private final LocalDate end = start.plusDays(3);

    private RentalRequestBuilder validBuilder() {
        return new RentalRequestBuilder()
                .customerId(" C-01 ")
                .vehicleId(" V-01 ")
                .dates(start, end)
                .rentalMode(new Economy());
    }

    @Test
    void buildsAValidRequestWithoutServices() {
        RentalRequest request = validBuilder().build();

        assertEquals("C-01", request.getCustomerId());
        assertEquals("V-01", request.getVehicleId());
        assertEquals(start, request.getStartDate());
        assertEquals(end, request.getEndDate());
        assertInstanceOf(Economy.class, request.getRentalMode());
        assertTrue(request.getServiceCodes().isEmpty());
    }

    @Test
    void includesSelectedServicesInOrderWithoutDuplicates() {
        RentalRequest request = validBuilder()
                .addService(" GPS ")
                .addService("CHILD_SEAT")
                .addService("GPS")
                .build();

        assertEquals(
                List.of("GPS", "CHILD_SEAT"),
                request.getServiceCodes()
        );
    }

    @Test
    void rejectsMissingCustomerId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().customerId(" ").build()
        );
    }

    @Test
    void rejectsMissingVehicleId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().vehicleId(null).build()
        );
    }

    @Test
    void rejectsMissingDates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().dates(null, end).build()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().dates(start, null).build()
        );
    }

    @Test
    void rejectsSameDayRental() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().dates(start, start).build()
        );
    }

    @Test
    void rejectsReversedDates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().dates(end, start).build()
        );
    }

    @Test
    void rejectsPastStartDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder()
                        .dates(LocalDate.now().minusDays(1), end)
                        .build()
        );
    }

    @Test
    void rejectsMissingRentalMode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().rentalMode(null).build()
        );
    }

    @Test
    void rejectsBlankServiceCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> validBuilder().addService("  ")
        );
    }

    @Test
    void builtRequestDoesNotChangeWhenBuilderIsReused() {
        RentalRequestBuilder builder =
                validBuilder().addService("GPS");

        RentalRequest firstRequest = builder.build();

        builder.addService("CHILD_SEAT");

        assertEquals(
                List.of("GPS"),
                firstRequest.getServiceCodes()
        );

        assertEquals(
                List.of("GPS", "CHILD_SEAT"),
                builder.build().getServiceCodes()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> firstRequest.getServiceCodes().add("DRIVER")
        );
    }
}
