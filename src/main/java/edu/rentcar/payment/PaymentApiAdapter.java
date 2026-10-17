
package edu.rentcar.payment;

import com.rentcar.paymentclient.PaymentApiClient;
import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;

import java.io.IOException;
import java.util.Objects;

public final class PaymentApiAdapter implements PaymentGateway {

    @FunctionalInterface
    public interface PaymentTransport {
        PaymentApiResponse send(PaymentApiRequest request)
                throws IOException, InterruptedException;
    }

    private final PaymentTransport transport;

    public PaymentApiAdapter(String baseUrl) {
        this(new PaymentApiClient(baseUrl)::pay);
    }

    // Allows isolated tests without starting the API.
    public PaymentApiAdapter(PaymentTransport transport) {
        this.transport = Objects.requireNonNull(
                transport,
                "Payment transport is required."
        );
    }

    @Override
    public PaymentResult pay(PaymentOrder order) {

        Objects.requireNonNull(
                order,
                "Payment order is required."
        );

        PaymentApiRequest externalRequest =
                new PaymentApiRequest(
                        order.reservationId(),
                        order.customerId(),
                        order.amount(),
                        order.paymentToken()
                );

        try {
            PaymentApiResponse response =
                    transport.send(externalRequest);

            if (response != null && response.httpStatus() == 200) {

                if (response.isApproved()) {
                    return new PaymentResult(
                            PaymentResult.Status.APPROVED,
                            "Payment approved."
                    );
                }

                if (response.isDeclined()) {
                    return new PaymentResult(
                            PaymentResult.Status.DECLINED,
                            "Payment declined."
                    );
                }
            }

            return new PaymentResult(
                    PaymentResult.Status.ERROR,
                    "Payment provider returned an error."
            );

        } catch (IOException e) {
            return new PaymentResult(
                    PaymentResult.Status.ERROR,
                    "Payment connection failed."
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return new PaymentResult(
                    PaymentResult.Status.ERROR,
                    "Payment request was interrupted."
            );
        }
    }
}
