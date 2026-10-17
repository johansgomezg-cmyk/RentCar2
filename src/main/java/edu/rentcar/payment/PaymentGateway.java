
package edu.rentcar.payment;

public interface PaymentGateway {

    PaymentResult pay(PaymentOrder order);
}
