
package edu.rentcar.model;

import edu.rentcar.service.RentalPricingService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ServiceChargeDecoratorTest {

    private RentalCharge baseRental() {
        RentalQuote quote = new RentalQuote(
                3,
                450000,
                "Basic coverage",
                "Business-hours assistance"
        );

        return new BaseRentalCharge(
                quote, "Toyota Corolla"
        );
    }

    private final IndividualService gps =
            new IndividualService("GPS", "GPS", 12000);

    private final IndividualService childSeat =
            new IndividualService("CHILD_SEAT", "Child seat", 8000);

    private final IndividualService extraDriver =
            new IndividualService("EXTRA_DRIVER", "Extra driver", 18000);

    @Test
    void calculatesBaseRentalCost() {
        RentalCharge rental = baseRental();

        assertEquals(450000, rental.totalCost(), 0.01);
        assertEquals(3, rental.rentalDays());
    }

    @Test
    void addsGpsCostToRental() {
        RentalCharge rental = new ServiceChargeDecorator(
                baseRental(), gps
        );

        assertEquals(486000, rental.totalCost(), 0.01);
    }

    @Test
    void supportsMultipleDecorators() {
        RentalCharge rental = baseRental();

        rental = new ServiceChargeDecorator(rental, gps);
        rental = new ServiceChargeDecorator(rental, childSeat);

        assertEquals(510000, rental.totalCost(), 0.01);
    }

    @Test
    void supportsCompositePackages() {
        ServicePackage family = new ServicePackage("Family")
                .add(childSeat)
                .add(extraDriver);

        ServicePackage complete = new ServicePackage("Complete")
                .add(gps)
                .add(family);

        RentalCharge rental = new ServiceChargeDecorator(
                baseRental(), complete
        );

        assertEquals(564000, rental.totalCost(), 0.01);
        assertTrue(rental.description().contains("Complete"));
        assertTrue(rental.description().contains("GPS"));
    }

    @Test
    void includesServicesInDescription() {
        RentalCharge rental = new ServiceChargeDecorator(
                baseRental(), gps
        );

        assertEquals(
                "Toyota Corolla rental (3 days) + GPS",
                rental.description()
        );
    }

    @Test
    void doesNotModifyOriginalRental() {
        RentalCharge original = baseRental();

        RentalCharge decorated =
                new ServiceChargeDecorator(original, gps);

        assertEquals(450000, original.totalCost(), 0.01);
        assertEquals(486000, decorated.totalCost(), 0.01);
    }

    @Test
    void rejectsMissingDependencies() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceChargeDecorator(null, gps)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ServiceChargeDecorator(baseRental(), null)
        );
    }

    @Test
    void integratesWithAbstractFactoryQuote() {
        LocalDate start = LocalDate.now().plusDays(2);

        Vehicle vehicle = new Vehicle(
                "VEH-1001", "Toyota", "Corolla",
                "Economy", 2024, 150000, "Available"
        );

        RentalRequest request = new RentalRequestBuilder()
                .customerId("CUS-1001")
                .vehicleId("VEH-1001")
                .dates(start, start.plusDays(3))
                .rentalMode(new Executive())
                .build();

        RentalQuote quote = new RentalPricingService()
                .calculateQuote(request, vehicle);

        RentalCharge rental = new ServiceChargeDecorator(
                new BaseRentalCharge(
                        quote, vehicle.getDisplayName()
                ),
                gps
        );

        assertEquals(531000, rental.totalCost(), 0.01);
    }
}
