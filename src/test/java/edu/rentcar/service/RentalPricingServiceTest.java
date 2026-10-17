
package edu.rentcar.service;

import edu.rentcar.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RentalPricingServiceTest {

    private final RentalPricingService service =
            new RentalPricingService();

    private Vehicle vehicle() {
        return new Vehicle(
                "VEH-1001",
                "Toyota",
                "Corolla",
                "Economy",
                2024,
                150000,
                "Available"
        );
    }

    private RentalRequest request(Rentalmode mode) {
        LocalDate start = LocalDate.now().plusDays(2);

        return new RentalRequestBuilder()
                .customerId("CUS-1001")
                .vehicleId("VEH-1001")
                .dates(start, start.plusDays(3))
                .rentalMode(mode)
                .build();
    }

    @Test
    void economyUsesBaseRate() {
        RentalQuote quote = service.calculateQuote(
                request(new Economy()), vehicle()
        );

        assertEquals(3, quote.rentalDays());
        assertEquals(450000, quote.rentalCost(), 0.01);
        assertFalse(quote.coverageDescription().isBlank());
        assertFalse(quote.assistanceDescription().isBlank());
    }

    @Test
    void executiveIncreasesPriceByTenPercent() {
        RentalQuote quote = service.calculateQuote(
                request(new Executive()), vehicle()
        );

        assertEquals(495000, quote.rentalCost(), 0.01);
    }

    @Test
    void premiumIncreasesPriceByFiftyPercent() {
        RentalQuote quote = service.calculateQuote(
                request(new Premium()), vehicle()
        );

        assertEquals(675000, quote.rentalCost(), 0.01);
    }

    @Test
    void newModeDoesNotRequireChangingPricingService() {

        Rentalmode promotional = new Rentalmode() {
            @Override
            public TariffPolicy createTariffPolicy() {
                return (dailyRate, days) ->
                        dailyRate * days * 0.80;
            }

            @Override
            public CoveragePolicy createCoveragePolicy() {
                return () -> "Promotional coverage";
            }

            @Override
            public AssistancePolicy createAssistancePolicy() {
                return () -> "Priority assistance";
            }
        };

        RentalQuote quote = service.calculateQuote(
                request(promotional), vehicle()
        );

        assertEquals(360000, quote.rentalCost(), 0.01);
        assertEquals(
                "Promotional coverage",
                quote.coverageDescription()
        );
    }

    @Test
    void rejectsDifferentVehicle() {
        Vehicle anotherVehicle = new Vehicle(
                "VEH-9999",
                "Mazda",
                "CX-5",
                "Executive",
                2025,
                230000,
                "Available"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateQuote(
                        request(new Economy()), anotherVehicle
                )
        );
    }
}
