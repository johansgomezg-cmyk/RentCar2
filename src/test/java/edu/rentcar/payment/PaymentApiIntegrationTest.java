
package edu.rentcar.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(
        named = "RENTCAR_INTEGRATION",
        matches = "true"
)
class PaymentApiIntegrationTest {

    private final PaymentGateway gateway =
            new PaymentApiAdapter("http://localhost:8080");

    private PaymentOrder order(String token) {
        return new PaymentOrder(
                "RES-1001",
                "CUS-1001",
                350000,
                token
        );
    }

    @Test
    void realApiApprovesPayment() {
        PaymentResult result = gateway.pay(
                order("tok_approved")
        );

        assertEquals(
                PaymentResult.Status.APPROVED,
                result.status()
        );
    }

    @Test
    void realApiDeclinesPayment() {
        PaymentResult result = gateway.pay(
                order("tok_declined")
        );

        assertEquals(
                PaymentResult.Status.DECLINED,
                result.status()
        );
    }

    @Test
    void realApiHandlesTechnicalError() {
        PaymentResult result = gateway.pay(
                order("tok_api_error")
        );

        assertEquals(
                PaymentResult.Status.ERROR,
                result.status()
        );
    }
}
