
package edu.rentcar.payment;

import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class PaymentApiAdapterTest {

    private PaymentOrder order(String token) {
        return new PaymentOrder(
                "RES-1001",
                "CUS-1001",
                350000,
                token
        );
    }

    @Test
    void mapsRequestAndApprovesPayment() {
        AtomicReference<PaymentApiRequest> captured =
                new AtomicReference<>();

        PaymentGateway gateway = new PaymentApiAdapter(request -> {
            captured.set(request);
            return new PaymentApiResponse(200, "APPROVED");
        });

        PaymentResult result = gateway.pay(order("tok_approved"));

        assertEquals(
                PaymentResult.Status.APPROVED,
                result.status()
        );

        assertEquals("RES-1001", captured.get().reservationId());
        assertEquals("CUS-1001", captured.get().customerId());
        assertEquals(350000, captured.get().amount(), 0.01);
        assertEquals("tok_approved", captured.get().paymentToken());
    }

    @Test
    void handlesDeclinedPayment() {
        PaymentGateway gateway = new PaymentApiAdapter(
                request -> new PaymentApiResponse(
                        200, "NOT_APPROVED")
        );

        PaymentResult result = gateway.pay(order("tok_declined"));

        assertEquals(
                PaymentResult.Status.DECLINED,
                result.status()
        );
    }

    @Test
    void handlesHttpError() {
        PaymentGateway gateway = new PaymentApiAdapter(
                request -> new PaymentApiResponse(
                        503, "Service unavailable")
        );

        assertEquals(
                PaymentResult.Status.ERROR,
                gateway.pay(order("tok_api_error")).status()
        );
    }

    @Test
    void rejectsUnexpectedProviderResponse() {
        PaymentGateway gateway = new PaymentApiAdapter(
                request -> new PaymentApiResponse(
                        200, "UNKNOWN")
        );

        assertEquals(
                PaymentResult.Status.ERROR,
                gateway.pay(order("tok_approved")).status()
        );
    }

    @Test
    void handlesConnectionFailure() {
        PaymentGateway gateway = new PaymentApiAdapter(
                request -> {
                    throw new IOException("Connection refused");
                }
        );

        assertEquals(
                PaymentResult.Status.ERROR,
                gateway.pay(order("tok_approved")).status()
        );
    }

    @Test
    void handlesInterruptedPayment() {
        PaymentGateway gateway = new PaymentApiAdapter(
                request -> {
                    throw new InterruptedException("Interrupted");
                }
        );

        try {
            PaymentResult result = gateway.pay(order("tok_approved"));

            assertEquals(PaymentResult.Status.ERROR, result.status());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void rejectsInvalidPaymentOrders() {
        assertThrows(
                IllegalArgumentException.class,
                () -> order("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentOrder(
                        "RES-1001",
                        "CUS-1001",
                        -100,
                        "tok_approved")
        );
    }
}
