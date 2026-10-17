
package edu.rentcar.model;

import edu.rentcar.enums.StatusReservation;
import edu.rentcar.payment.PaymentResult;

import java.util.Objects;

public record CheckoutResult(
        Reservation reservation,
        RentalEstimate estimate,
        PaymentResult payment
) {
    public CheckoutResult {
        Objects.requireNonNull(reservation);
        Objects.requireNonNull(estimate);
        Objects.requireNonNull(payment);
    }

    public boolean isConfirmed() {
        return StatusReservation.Confirmed.name()
                .equals(reservation.getStatus());
    }
}
