
package edu.rentcar.service;

import edu.rentcar.data.SampleData;
import edu.rentcar.enums.StatusReservation;
import edu.rentcar.model.*;
import edu.rentcar.payment.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RentalFacadeCheckoutTest {

    private final ReservationService reservations =
            new ReservationService();

    private RentalFacade facade(PaymentGateway gateway) {
        return new RentalFacade(
                SampleData.vehicles(),
                reservations,
                new RentalPricingService(),
                new VehicleAvailabilityService(),
                RentalServiceCatalog.standardServices(),
                gateway
        );
    }

    private RentalRequest request(String customerId) {
        LocalDate start = LocalDate.now().plusDays(2);

        return new RentalRequestBuilder()
                .customerId(customerId)
                .vehicleId("VEH-1001")
                .dates(start, start.plusDays(3))
                .rentalMode(new Economy())
                .addService("GPS")
                .build();
    }

    private PaymentGateway protectedGateway(
            PaymentGateway realGateway) {

        return new PaymentVerificationProxy(
                new InMemoryCustomerVerificationService(
                        SampleData.customers()
                ),
                realGateway
        );
    }

    @Test
    void approvedPaymentConfirmsReservation() {

        AtomicReference<PaymentOrder> captured =
                new AtomicReference<>();

        PaymentGateway gateway = protectedGateway(order -> {
            captured.set(order);
            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Payment approved."
            );
        });

        CheckoutResult result = facade(gateway).checkout(
                request("CUS-1001"),
                "tok_approved"
        );

        assertTrue(result.isConfirmed());

        assertEquals(
                StatusReservation.Confirmed.name(),
                result.reservation().getStatus()
        );

        assertEquals(486000, captured.get().amount(), 0.01);

        assertEquals(
                result.reservation().getId(),
                captured.get().reservationId()
        );

        assertEquals(1, reservations.getAllReservations().size());
    }

    @Test
    void declinedPaymentDoesNotConfirmReservation() {

        PaymentGateway gateway = protectedGateway(order ->
                new PaymentResult(
                        PaymentResult.Status.DECLINED,
                        "Payment declined."
                )
        );

        CheckoutResult result = facade(gateway).checkout(
                request("CUS-1001"),
                "tok_declined"
        );

        assertFalse(result.isConfirmed());

        assertEquals(
                StatusReservation.PaymentDeclined.name(),
                result.reservation().getStatus()
        );

        assertEquals(1, reservations.getAllReservations().size());
    }

    @Test
    void unauthorizedCustomerNeverReachesPaymentApi() {

        AtomicInteger calls = new AtomicInteger();

        PaymentGateway externalGateway = order -> {
            calls.incrementAndGet();
            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Payment approved."
            );
        };

        CheckoutResult result = facade(
                protectedGateway(externalGateway)
        ).checkout(
                request("CUS-1003"),
                "tok_approved"
        );

        assertFalse(result.isConfirmed());
        assertEquals(0, calls.get());

        assertEquals(
                StatusReservation.PaymentDeclined.name(),
                result.reservation().getStatus()
        );
    }

    @Test
    void technicalErrorDoesNotConfirmReservation() {

        PaymentGateway gateway = protectedGateway(order ->
                new PaymentResult(
                        PaymentResult.Status.ERROR,
                        "Provider unavailable."
                )
        );

        CheckoutResult result = facade(gateway).checkout(
                request("CUS-1001"),
                "tok_api_error"
        );

        assertFalse(result.isConfirmed());

        assertEquals(
                StatusReservation.PaymentError.name(),
                result.reservation().getStatus()
        );
    }

    @Test
    void failedPaymentDoesNotBlockVehicle() {

        RentalFacade facade = facade(protectedGateway(order ->
                new PaymentResult(
                        PaymentResult.Status.DECLINED,
                        "Declined."
                )
        ));

        facade.checkout(
                request("CUS-1001"),
                "tok_declined"
        );

        assertDoesNotThrow(() ->
                facade.quote(request("CUS-1002"))
        );
    }

    @Test
    void rejectsMissingPaymentTokenWithoutCreatingReservation() {

        AtomicInteger calls = new AtomicInteger();

        RentalFacade facade = facade(order -> {
            calls.incrementAndGet();
            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Approved."
            );
        });

        assertThrows(
                IllegalArgumentException.class,
                () -> facade.checkout(request("CUS-1001"), "")
        );

        assertEquals(0, calls.get());
        assertTrue(reservations.getAllReservations().isEmpty());
    }

    @Test
    void approvedPaymentWithAvailabilityConflictRequiresReview() {

        PaymentGateway gateway = protectedGateway(order -> {

            LocalDate start = LocalDate.now().plusDays(2);

            Reservation other = reservations.createReservation(
                    "CUS-1002",
                    "VEH-1001",
                    start,
                    start.plusDays(3)
            );

            reservations.confirmReservation(other.getId());

            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Payment approved."
            );
        });

        CheckoutResult result = facade(gateway).checkout(
                request("CUS-1001"),
                "tok_approved"
        );

        assertFalse(result.isConfirmed());

        assertEquals(
                StatusReservation.PaymentError.name(),
                result.reservation().getStatus()
        );

        assertTrue(
                result.payment().message().contains("Manual payment review")
        );
    }
}
