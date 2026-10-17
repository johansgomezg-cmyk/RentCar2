
package edu.rentcar.payment;

import java.util.Objects;

public final class PaymentVerificationProxy
        implements PaymentGateway {

    private final CustomerAuthorizationService verificationService;
    private final PaymentGateway realGateway;

    public PaymentVerificationProxy(
            CustomerAuthorizationService verificationService,
            PaymentGateway realGateway) {

        this.verificationService = Objects.requireNonNull(
                verificationService,
                "Verification service is required."
        );

        this.realGateway = Objects.requireNonNull(
                realGateway,
                "Payment gateway is required."
        );
    }

    @Override
    public PaymentResult pay(PaymentOrder order) {

        Objects.requireNonNull(
                order,
                "Payment order is required."
        );

        final boolean authorized;

        try {
            authorized = verificationService.isAuthorized(
                    order.customerId()
            );
        } catch (RuntimeException e) {
            return new PaymentResult(
                    PaymentResult.Status.ERROR,
                    "Customer verification is unavailable."
            );
        }

        if (!authorized) {
            return new PaymentResult(
                    PaymentResult.Status.DECLINED,
                    "Customer is not authorized."
            );
        }

        return realGateway.pay(order);
    }
}
