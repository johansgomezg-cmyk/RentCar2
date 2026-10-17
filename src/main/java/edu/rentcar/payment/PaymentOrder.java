
package edu.rentcar.payment;

public record PaymentOrder(
        String reservationId,
        String customerId,
        double amount,
        String paymentToken
) {
    public PaymentOrder {
        if (reservationId == null || reservationId.isBlank()
                || customerId == null || customerId.isBlank()
                || paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment identifiers and token are required.");
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be positive.");
        }

        reservationId = reservationId.trim();
        customerId = customerId.trim();
        paymentToken = paymentToken.trim();
    }
}
