
package edu.rentcar.payment;

import edu.rentcar.data.SampleData;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PaymentVerificationProxyTest {

    private PaymentOrder order(String customerId) {
        return new PaymentOrder(
                "RES-1001",
                customerId,
                350000,
                "tok_approved"
        );
    }

    private CustomerAuthorizationService verifier() {
        return new InMemoryCustomerVerificationService(
                SampleData.customers()
        );
    }

    @Test
    void authorizedCustomerCanPay() {
        AtomicInteger calls = new AtomicInteger();

        PaymentGateway gateway = request -> {
            calls.incrementAndGet();
            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Payment approved."
            );
        };

        PaymentGateway proxy =
                new PaymentVerificationProxy(verifier(), gateway);

        PaymentResult result = proxy.pay(order("CUS-1001"));

        assertEquals(
                PaymentResult.Status.APPROVED,
                result.status()
        );

        assertEquals(1, calls.get());
    }

    @Test
    void unauthorizedCustomerNeverCallsGateway() {
        AtomicInteger calls = new AtomicInteger();

        PaymentGateway gateway = request -> {
            calls.incrementAndGet();
            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Payment approved."
            );
        };

        PaymentGateway proxy =
                new PaymentVerificationProxy(verifier(), gateway);

        PaymentResult result = proxy.pay(order("CUS-1003"));

        assertEquals(
                PaymentResult.Status.DECLINED,
                result.status()
        );

        assertEquals(0, calls.get());
    }

    @Test
    void unknownCustomerCannotPay() {
        AtomicInteger calls = new AtomicInteger();

        PaymentGateway gateway = request -> {
            calls.incrementAndGet();
            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Payment approved."
            );
        };

        PaymentGateway proxy =
                new PaymentVerificationProxy(verifier(), gateway);

        PaymentResult result = proxy.pay(order("UNKNOWN"));

        assertEquals(
                PaymentResult.Status.DECLINED,
                result.status()
        );

        assertEquals(0, calls.get());
    }

    @Test
    void verificationFailurePreventsPayment() {
        AtomicInteger calls = new AtomicInteger();

        CustomerAuthorizationService failingService =
                customerId -> {
                    throw new IllegalStateException(
                            "Verification unavailable"
                    );
                };

        PaymentGateway gateway = request -> {
            calls.incrementAndGet();
            return new PaymentResult(
                    PaymentResult.Status.APPROVED,
                    "Payment approved."
            );
        };

        PaymentGateway proxy =
                new PaymentVerificationProxy(
                        failingService, gateway
                );

        PaymentResult result = proxy.pay(order("CUS-1001"));

        assertEquals(
                PaymentResult.Status.ERROR,
                result.status()
        );

        assertEquals(0, calls.get());
    }

    @Test
    void propagatesDeclinedPaymentForAuthorizedCustomer() {
        AtomicInteger calls = new AtomicInteger();

        PaymentGateway gateway = request -> {
            calls.incrementAndGet();
            return new PaymentResult(
                    PaymentResult.Status.DECLINED,
                    "Payment declined."
            );
        };

        PaymentGateway proxy =
                new PaymentVerificationProxy(verifier(), gateway);

        PaymentResult result = proxy.pay(order("CUS-1001"));

        assertEquals(
                PaymentResult.Status.DECLINED,
                result.status()
        );

        assertEquals(1, calls.get());
    }

    @Test
    void rejectsMissingDependencies() {
        PaymentGateway gateway = request ->
                new PaymentResult(
                        PaymentResult.Status.APPROVED,
                        "Payment approved."
                );

        assertThrows(
                NullPointerException.class,
                () -> new PaymentVerificationProxy(null, gateway)
        );

        assertThrows(
                NullPointerException.class,
                () -> new PaymentVerificationProxy(verifier(), null)
        );
    }
}
