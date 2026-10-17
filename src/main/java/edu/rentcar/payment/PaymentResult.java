
package edu.rentcar.payment;

public record PaymentResult(
        Status status,
        String message
) {
    public enum Status {
        APPROVED,
        DECLINED,
        ERROR
    }

    public PaymentResult {
        if (status == null || message == null) {
            throw new IllegalArgumentException(
                    "Payment result is incomplete.");
        }
    }
}
