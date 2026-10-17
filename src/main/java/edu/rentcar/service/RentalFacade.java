
package edu.rentcar.service;

import edu.rentcar.enums.StatusReservation;
import edu.rentcar.model.*;
import edu.rentcar.payment.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RentalFacade {

    private final List<Vehicle> vehicles;
    private final ReservationService reservationService;
    private final RentalPricingService pricingService;
    private final VehicleAvailabilityService availabilityService;
    private final Map<String, RentalServiceItem> serviceCatalog;
    private final PaymentGateway paymentGateway;

    // Keeps existing quote-only tests compatible.
    public RentalFacade(
            List<Vehicle> vehicles,
            ReservationService reservationService,
            RentalPricingService pricingService,
            VehicleAvailabilityService availabilityService,
            Map<String, RentalServiceItem> serviceCatalog) {

        this(
                vehicles,
                reservationService,
                pricingService,
                availabilityService,
                serviceCatalog,
                null
        );
    }

    // Full constructor for checkout operations.
    public RentalFacade(
            List<Vehicle> vehicles,
            ReservationService reservationService,
            RentalPricingService pricingService,
            VehicleAvailabilityService availabilityService,
            Map<String, RentalServiceItem> serviceCatalog,
            PaymentGateway paymentGateway) {

        this.vehicles = List.copyOf(vehicles);
        this.reservationService = Objects.requireNonNull(
                reservationService);
        this.pricingService = Objects.requireNonNull(
                pricingService);
        this.availabilityService = Objects.requireNonNull(
                availabilityService);
        this.serviceCatalog = Map.copyOf(serviceCatalog);
        this.paymentGateway = paymentGateway;
    }

    public RentalEstimate quote(RentalRequest request) {

        Objects.requireNonNull(request, "Request is required.");

        Vehicle vehicle = vehicles.stream()
                .filter(v -> v.getId().equals(request.getVehicleId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Vehicle not found."));

        if (!"Available".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalStateException(
                    "Vehicle is not active.");
        }

        if (!availabilityService.isAvailable(
                vehicle.getId(),
                request.getStartDate(),
                request.getEndDate(),
                reservationService.getAllReservations())) {

            throw new IllegalStateException(
                    "Vehicle is unavailable.");
        }

        RentalQuote baseQuote =
                pricingService.calculateQuote(request, vehicle);

        RentalCharge charge = new BaseRentalCharge(
                baseQuote,
                vehicle.getDisplayName()
        );

        Set<String> includedServices = new HashSet<>();

        for (String code : request.getServiceCodes()) {

            RentalServiceItem service = serviceCatalog.get(code);

            if (service == null) {
                throw new IllegalArgumentException(
                        "Unknown service: " + code);
            }

            validateNoDuplicateServices(
                    service, includedServices
            );

            charge = new ServiceChargeDecorator(charge, service);
        }

        return new RentalEstimate(
                vehicle.getId(),
                charge.rentalDays(),
                charge.totalCost(),
                charge.description(),
                baseQuote.coverageDescription(),
                baseQuote.assistanceDescription()
        );
    }

    // Coordinates reservation, authorization and payment.
    public CheckoutResult checkout(
            RentalRequest request,
            String paymentToken) {

        if (paymentGateway == null) {
            throw new IllegalStateException(
                    "Payment gateway is not configured.");
        }

        if (paymentToken == null || paymentToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment token is required.");
        }

        // Validate and calculate before creating the reservation.
        RentalEstimate estimate = quote(request);

        Reservation pending = reservationService.createReservation(
                request.getCustomerId(),
                request.getVehicleId(),
                request.getStartDate(),
                request.getEndDate()
        );

        PaymentResult payment;

        try {
            PaymentOrder order = new PaymentOrder(
                    pending.getId(),
                    request.getCustomerId(),
                    estimate.totalCost(),
                    paymentToken
            );

            // Must be a PaymentVerificationProxy in the real app.
            payment = paymentGateway.pay(order);

            if (payment == null) {
                payment = new PaymentResult(
                        PaymentResult.Status.ERROR,
                        "Payment gateway returned no result."
                );
            }

        } catch (RuntimeException exception) {
            payment = new PaymentResult(
                    PaymentResult.Status.ERROR,
                    "Payment processing failed."
            );
        }

        if (payment.status() == PaymentResult.Status.APPROVED) {

            try {
                Reservation confirmed =
                        reservationService.confirmReservation(
                                pending.getId());

                return new CheckoutResult(
                        confirmed, estimate, payment
                );

            } catch (IllegalStateException exception) {

                Reservation failed =
                        reservationService.markUnsuccessful(
                                pending.getId(),
                                StatusReservation.PaymentError
                        );

                return new CheckoutResult(
                        failed,
                        estimate,
                        new PaymentResult(
                                PaymentResult.Status.ERROR,
                                "Payment approved but reservation confirmation failed. "
                                        + "Manual payment review is required."
                        )
                );
            }
        }

        StatusReservation failureStatus =
                payment.status() == PaymentResult.Status.DECLINED
                        ? StatusReservation.PaymentDeclined
                        : StatusReservation.PaymentError;

        Reservation failed = reservationService.markUnsuccessful(
                pending.getId(),
                failureStatus
        );

        return new CheckoutResult(
                failed, estimate, payment
        );
    }

    private void validateNoDuplicateServices(
            RentalServiceItem item,
            Set<String> includedServices) {

        if (item instanceof IndividualService individual) {

            if (!includedServices.add(individual.code())) {
                throw new IllegalArgumentException(
                        "Duplicated service: " + individual.code());
            }

        } else if (item instanceof ServicePackage servicePackage) {

            for (RentalServiceItem component :
                    servicePackage.getComponents()) {

                validateNoDuplicateServices(
                        component, includedServices
                );
            }
        }
    }
}
