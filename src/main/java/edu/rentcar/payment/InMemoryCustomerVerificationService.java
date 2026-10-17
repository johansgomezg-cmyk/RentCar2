
package edu.rentcar.payment;

import edu.rentcar.model.Customer;
import java.util.List;
import java.util.Objects;

public final class InMemoryCustomerVerificationService
        implements CustomerAuthorizationService {

    private final List<Customer> customers;

    public InMemoryCustomerVerificationService(
            List<Customer> customers) {

        this.customers = List.copyOf(
                Objects.requireNonNull(
                        customers,
                        "Customers are required."
                )
        );
    }

    @Override
    public boolean isAuthorized(String customerId) {

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer ID is required.");
        }

        return customers.stream()
                .filter(customer ->
                        customer.getId().equals(customerId))
                .findFirst()
                .map(Customer::isAuthorized)
                .orElse(false);
    }
}
