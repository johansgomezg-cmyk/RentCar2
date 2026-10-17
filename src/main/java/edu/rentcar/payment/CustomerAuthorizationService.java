
package edu.rentcar.payment;

@FunctionalInterface
public interface CustomerAuthorizationService {

    boolean isAuthorized(String customerId);
}
