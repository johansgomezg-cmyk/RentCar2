
package edu.rentcar.model;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicePackageTest {

    private final IndividualService gps =
            new IndividualService("GPS", "GPS", 12000);

    private final IndividualService childSeat =
            new IndividualService("CHILD_SEAT", "Child seat", 8000);

    private final IndividualService extraDriver =
            new IndividualService("EXTRA_DRIVER", "Extra driver", 18000);

    @Test
    void calculatesIndividualServiceCost() {
        assertEquals(36000, gps.costForDays(3), 0.01);
    }

    @Test
    void calculatesSimplePackageCost() {
        ServicePackage family = new ServicePackage("Family pack")
                .add(childSeat)
                .add(extraDriver);

        assertEquals(78000, family.costForDays(3), 0.01);
    }

    @Test
    void calculatesNestedPackageCost() {
        ServicePackage family = new ServicePackage("Family pack")
                .add(childSeat)
                .add(extraDriver);

        ServicePackage complete = new ServicePackage("Complete pack")
                .add(gps)
                .add(family);

        assertEquals(114000, complete.costForDays(3), 0.01);
    }

    @Test
    void describesNestedPackage() {
        ServicePackage family = new ServicePackage("Family pack")
                .add(childSeat)
                .add(extraDriver);

        ServicePackage complete = new ServicePackage("Complete pack")
                .add(gps)
                .add(family);

        assertEquals(
                "Complete pack [GPS, Family pack [Child seat, Extra driver]]",
                complete.description()
        );
    }

    @Test
    void rejectsDirectCycle() {
        ServicePackage pack = new ServicePackage("Pack");

        assertThrows(
                IllegalArgumentException.class,
                () -> pack.add(pack)
        );
    }

    @Test
    void rejectsIndirectCycle() {
        ServicePackage first = new ServicePackage("First");
        ServicePackage second = new ServicePackage("Second");

        first.add(second);

        assertThrows(
                IllegalArgumentException.class,
                () -> second.add(first)
        );
    }

    @Test
    void rejectsDuplicateComponents() {
        ServicePackage pack = new ServicePackage("Pack")
                .add(gps);

        assertThrows(
                IllegalArgumentException.class,
                () -> pack.add(gps)
        );
    }

    @Test
    void rejectsInvalidDays() {
        ServicePackage pack = new ServicePackage("Pack")
                .add(gps);

        assertThrows(
                IllegalArgumentException.class,
                () -> pack.costForDays(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> gps.costForDays(-1)
        );
    }

    @Test
    void rejectsInvalidDailyRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new IndividualService(
                        "INVALID", "Invalid", -100)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new IndividualService(
                        "INVALID", "Invalid", Double.NaN)
        );
    }

    @Test
    void rejectsEmptyPackageCalculation() {
        ServicePackage empty = new ServicePackage("Empty");

        assertThrows(
                IllegalStateException.class,
                () -> empty.costForDays(3)
        );
    }

    @Test
    void protectsComponentCollection() {
        ServicePackage pack = new ServicePackage("Pack")
                .add(gps);

        List<RentalServiceItem> components =
                pack.getComponents();

        assertThrows(
                UnsupportedOperationException.class,
                () -> components.clear()
        );
    }
}
